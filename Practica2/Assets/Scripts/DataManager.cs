using System;
using System.Collections.Generic;
using UnityEngine;
using System.IO;
using System.Linq;
using System.Security.Cryptography;
using System.Text;

public static class DataManager
{
    public struct LevelData
    {
        public enum LevelState
        {
            UNCOMPLETED,
            COMPLETED,
            PERFECT
        }

        public string data;
        public int name;
        public string packName;
        public Color color;
        public int bestMovements;
        public LevelState state;

        public LevelData(string data, int name, string packName, Color color, int bestMovements = -1,
            LevelState state = LevelState.UNCOMPLETED)
        {
            this.data = data;
            this.name = name;
            this.packName = packName;
            this.color = color;
            this.bestMovements = bestMovements;
            this.state = state;
        }
    }

    public class PackData
    {
        public List<List<LevelData>> pagesData;
        public int completed;
        public string name;
        public bool locked;

        public PackData(string name)
        {
            pagesData = new List<List<LevelData>>();
            this.completed = 0;
            this.name = name;
            locked = false;
        }
    }

    private static readonly string path = Application.persistentDataPath + "/";
    private static List<PackData> _packsData = new List<PackData>();
    public static int clues = 3;

    public static void LoadSaveData(List<PackGroup> groups)
    {
        if (_packsData.Count != 0)
            return;

        string cluesPath = path + "Playerclues.txt";
        if (File.Exists(cluesPath))
        {
            List<string> cluesData = File.ReadAllLines(cluesPath).ToList();
            if (CompareSha256(cluesData, SystemInfo.deviceModel + "JajaLoHasRoto"))
                clues = Int32.Parse(cluesData[0]);
            else
                clues = 0;
        }
        
        foreach (PackGroup group in groups)
        {
            foreach (var pack in group.packs)
            {
                PackData packData = new PackData(pack.name);
                packData.locked = pack.locked;

                string[] data = pack.file.ToString().Split('\n');
                for (int i = 0; i < pack.pages.Length; i++)
                {
                    List<LevelData> levelCellData = new List<LevelData>();
                    for (int j = 0; j < 30; j++)
                    {
                        string cellData = data[30 * i + j];
                        levelCellData.Add(new LevelData(cellData, ((i * 30) + j + 1), pack.name,
                            pack.pages[i].pageColor));
                    }

                    packData.pagesData.Add(levelCellData);
                }

                string openPath = path + pack.name + ".txt";
                if (File.Exists(openPath))
                {
                    // nCompletados (ej: 10)
                    // ..... nivel completado por linea (nivel estado nMovimientos)
                    //  nivel (1-150) /////// estado (0(perfecto) , 1(completado)) ///// nMovimientos (ej: 6)
                    List<string> lines = File.ReadAllLines(openPath).ToList();
                    if (CompareSha256(lines,SystemInfo.deviceModel))
                    {
                        int nFinished = int.Parse(lines[0]);
                        packData.completed = nFinished;
                        for (int i = 1; i <= nFinished; i++)
                        {
                            string[] line = lines[i].Split(' ');
                            int level = int.Parse(line[0]) - 1;
                            bool perfect = line[1] == "0";
                            int movements = int.Parse(line[2]);
                            int listPos = level / 30;
                            int gridPos = level - listPos * 30;
                            var levelCellData = packData.pagesData[listPos][gridPos];
                            levelCellData.bestMovements = movements;
                            levelCellData.state =
                                perfect ? LevelData.LevelState.PERFECT : LevelData.LevelState.COMPLETED;
                            packData.pagesData[listPos][gridPos] = levelCellData;
                        }
                    }
                }

                _packsData.Add(packData);
            }
        }
    }


    private static Vector2Int GetRealPos(int pos)
    {
        return new Vector2Int(pos / 30, pos - (pos / 30) * 30);
    }

    private static void GetPack(out PackData data, string packName)
    {
        foreach (PackData pack in _packsData)
        {
            if (pack.name == packName)
            {
                data = pack;
                return;
            }
        }

        data = null;
    }

    public static void LevelPassed(LevelData data)
    {
        GetPack(out PackData dataPack, data.packName);

        if (dataPack == null)
            throw new Exception("Couldn't find that level, oh oh");

        Vector2Int pos = GetRealPos(data.name - 1);
        var levelCellData = dataPack.pagesData[pos.x][pos.y];
        
        if(levelCellData.bestMovements == -1 || levelCellData.bestMovements > data.bestMovements)
            levelCellData.bestMovements = data.bestMovements;

        if (levelCellData.state == LevelData.LevelState.UNCOMPLETED && data.state != LevelData.LevelState.UNCOMPLETED)
            dataPack.completed++;

        levelCellData.state = data.state;

        dataPack.pagesData[pos.x][pos.y] = levelCellData;
    }

    public static bool ThereIsNextLevel(LevelData data, bool previous = false)
    {
        GetPack(out PackData dataPack, data.packName);

        if (dataPack == null)
            throw new Exception("No next level, oh oh");

        int max = dataPack.pagesData.Count * 30;
        int targetPos = data.name + (previous ? -2 : 0);

        return targetPos >= 0 && targetPos < max;
    }
    
    public static LevelData NextLevel(LevelData data, bool previous = false)
    {
        GetPack(out PackData dataPack, data.packName);

        if (dataPack == null)
            throw new Exception("No next level, oh oh");

        int max = dataPack.pagesData.Count * 30;
        int targetPos = data.name + (previous ? -2 : 0);

        int clampedPos = targetPos >= 0 ? targetPos % max : max + targetPos;

        Vector2Int pos = GetRealPos(clampedPos);

        return dataPack.pagesData[pos.x][pos.y];
    }

    public static PackData GetPackData(string name)
    {
        GetPack(out PackData dataPack, name);

        if (dataPack == null)
            throw new Exception("No pack found, oh oh");

        return dataPack;
    }


    public static void SaveCurrentData()
    {
        string cluesHash = ComputeSha256(clues.ToString() , SystemInfo.deviceModel + "JajaLoHasRoto");

        string[] cluesData = {cluesHash, clues.ToString()};
        
        File.WriteAllLines(path + "Playerclues.txt",cluesData);
        
        List<string> dataToSave = new List<string>();
        foreach (PackData pack in _packsData)
        {
            dataToSave.Clear();
            foreach (List<LevelData> page in pack.pagesData)
            {
                foreach (LevelData data in page)
                {
                    if (data.state != LevelData.LevelState.UNCOMPLETED)
                    {
                        string state = data.state == LevelData.LevelState.PERFECT ? "0" : "1";
                        dataToSave.Add(data.name.ToString() + " " + state + " " + data.bestMovements.ToString());
                    }
                }
            }

            if (dataToSave.Count != 0)
            {
                dataToSave.Insert(0, (dataToSave.Count).ToString());

                dataToSave.Insert(0, ComputeSha256(String.Join("", dataToSave),SystemInfo.deviceModel));

                File.WriteAllLines(path + pack.name + ".txt", dataToSave);
            }
        }
    }

    private static string ComputeSha256(string data, string endKey)
    {
        // Create a SHA256   
        using (SHA256 sha256Hash = SHA256.Create())
        {
            // ComputeHash - returns byte array  
            byte[] bytes = sha256Hash.ComputeHash(Encoding.UTF8.GetBytes(data));

            // Convert byte array to a string   
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < bytes.Length; i++)
            {
                builder.Append(bytes[i].ToString("x2"));
            }

            return builder.ToString();
        }
    }

    private static bool CompareSha256(List<string> data, string endKey)
    {
        string hash = data[0];
        data.RemoveAt(0);
        string hashCheck = ComputeSha256(String.Join("", data) , endKey);

        return hash == hashCheck;
    }
}