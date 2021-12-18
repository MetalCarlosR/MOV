using System;
using System.Collections.Generic;
using UnityEngine;
using System.IO;
using System.Linq;
using System.Security.Cryptography;
using System.Text;

public static class DataManager
{
    // Structure storing all the necesary data from a level so it can be showed in game
    // and also stored/saved
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
    

    // Simple data structure for a pack to be loaded and drawn in game
    // Is a class instead of a struct because of problems with the references when accessing one from a list
    // and nullifying the data in returns
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

    /// <summary>
    /// Loads the data from disk from all the groups that the game pass to him as well as
    /// the player data (right now only the clues left).
    /// It also creates the data for all the levels that haven't been completed so it can be
    /// marked as that by the game.
    /// The files should have a hash code at the beginning used to know if the last saved data has
    /// been modified, if the comparison fails the player looses all the progress.
    /// </summary>
    /// <param name="groups">List with the packs to be loaded</param>
    public static void LoadSaveData(List<PackGroup> groups)
    {
        // Prevent the Manager from loading everything more than once
        if (_packsData.Count != 0)
            return;

        // Loads the clues from disk
        string cluesPath = path + "Playerclues.txt";
        if (File.Exists(cluesPath))
        {
            List<string> cluesData = File.ReadAllLines(cluesPath).ToList();
            if (CompareSha256(cluesData, SystemInfo.deviceModel + "JajaLoHasRoto"))
                clues = Int32.Parse(cluesData[0]);
            else
                clues = 0;
        }
        
        
        // Creates the data for all the groups and loads the data found on disk
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
                

                // If a file associated with the current created pack is found loads the data
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

    
    /// <summary>
    /// Converter from cell index (1-x) to list index ([0-5][0-30])
    /// </summary>
    /// <param name="pos">Cell index</param>
    /// <returns></returns>
    private static Vector2Int GetRealPos(int pos)
    {
        return new Vector2Int(pos / 30, pos - (pos / 30) * 30);
    }
    
    /// <summary>
    /// Stores in out the pack with the name pass in parameter.
    /// If not found stores null.
    /// </summary>
    /// <param name="data">out PackData for storing</param>
    /// <param name="packName">Name of pack to be found</param>
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

    /// <summary>
    /// Updates the data from the level passed in parameter when is completed
    /// so it can be stored and saved later when the game is closed.
    /// </summary>
    /// <param name="data">New data from level</param>
    /// <exception cref="Exception">Pack associated with the level not found</exception>
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

    /// <summary>
    /// Easy check for the game to know if there is a next level.
    /// Works in both ways, will always be true except in the last level
    /// asking for the next and the first level asking for the previous.
    /// </summary>
    /// <param name="data">Level to use as "center"</param>
    /// <param name="previous">Direction to look into ( previous/next)</param>
    /// <returns>True if there is, false otherwise</returns>
    /// <exception cref="Exception">Pack associated with the level not found</exception>
    public static bool ThereIsNextLevel(LevelData data, bool previous = false)
    {
        GetPack(out PackData dataPack, data.packName);

        if (dataPack == null)
            throw new Exception("No next level, oh oh");

        int max = dataPack.pagesData.Count * 30;
        int targetPos = data.name + (previous ? -2 : 0);

        return targetPos >= 0 && targetPos < max;
    }
    
    
    /// <summary>
    /// Returns the consecutive level to the one given in parameter, works for both
    /// directions.
    /// To prevent null returns or out of range accesses if it reaches the end it cycles
    /// all the way to the other end and returns that firs/last element.
    /// </summary>
    /// <param name="data">Level to use as "center"</param>
    /// <param name="previous">Direction to look into ( previous/next)</param>
    /// <returns>Next level in said direction</returns>
    /// <exception cref="Exception">Pack associated with the level not found</exception>
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

    /// <summary>
    /// Returns pack with the name passed by parameter.
    /// Throws an exception if not found.
    /// </summary>
    /// <param name="name">Name of the pack to find</param>
    /// <returns>Pack found with said name</returns>
    /// <exception cref="Exception">Pack with that name not found</exception>
    public static PackData GetPackData(string name)
    {
        GetPack(out PackData dataPack, name);

        if (dataPack == null)
            throw new Exception("No pack found, oh oh");

        return dataPack;
    }


    /// <summary>
    /// Serializes and stores all the data from the game in text files for later
    /// load when the game is reopened.
    /// Only stores the data that has some value, levels that aren't completed are not stored
    /// because they have default values.
    /// To all the text files a hash is added to prevent the user from changing the data and unlocking
    /// content without playing. The "encryption" method uses all the data in the text files and adds extra
    /// text like the device model and some random text. Is pretty easy to break, if we wanted to give real
    /// security we would have the data stored in servers or using an external servers to generate the keys
    /// for the "encryption".
    /// </summary>
    public static void SaveCurrentData()
    {
        // Hash for the clues text file
        string cluesHash = ComputeSha256(clues.ToString() , SystemInfo.deviceModel + "JajaLoHasRoto");

        string[] cluesData = {cluesHash, clues.ToString()};
        
        File.WriteAllLines(path + "Playerclues.txt",cluesData);
        
        // Store the data from all the packs
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

            // If nothing of value is to be saved it ignores the file and doesn't create it
            if (dataToSave.Count != 0)
            {
                dataToSave.Insert(0, (dataToSave.Count).ToString());

                dataToSave.Insert(0, ComputeSha256(String.Join("", dataToSave),SystemInfo.deviceModel));

                File.WriteAllLines(path + pack.name + ".txt", dataToSave);
            }
        }
    }

    /// <summary>
    /// Creates a hash with an original text, adding a "key" at the end to make the "decryption" more difficult
    /// </summary>
    /// <param name="data">Original text</param>
    /// <param name="endKey">Key to add to the text</param>
    /// <returns>Hash in string format</returns>
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

    /// <summary>
    /// Compares a hash stored in the first position with another hash generated from an original text and a key.
    /// </summary>
    /// <param name="data">Original data</param>
    /// <param name="endKey">Key</param>
    /// <returns>True if generated hash is equal to the parameter hash, False otherwise</returns>
    private static bool CompareSha256(List<string> data, string endKey)
    {
        string hash = data[0];
        data.RemoveAt(0);
        string hashCheck = ComputeSha256(String.Join("", data) , endKey);

        return hash == hashCheck;
    }
}