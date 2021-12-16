using System;
using System.Collections.Generic;
using UnityEngine;
using System.IO;

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

   public struct PackData
   {
      public List<List<LevelData>> pagesData;
      public int completed;
      public string name;

      public PackData(string name)
      {
         pagesData = new List<List<LevelData>>();
         this.completed = 0;
         this.name = name;
      }
   }
   
   private static readonly string path = Application.persistentDataPath + "/";
   private static List<PackData> _packsData = new List<PackData>();

   public static void LoadSaveData(List<PackGroup> groups)
   {
      if (_packsData.Count != 0)
         return;
      
      foreach (PackGroup group in groups)
      {
         foreach (var pack in group.packs)
         {
            PackData packData = new PackData(pack.name);

            string[] data = pack.file.ToString().Split('\n');
            for (int i = 0; i < pack.pages.Length; i++)
            {
               List<LevelData> levelCellData = new List<LevelData>();
               for (int j = 0; j < 30; j++)
               {
                  string cellData = data[30 * i + j];
                  levelCellData.Add(new LevelData(cellData,((i*30)+ j+1),pack.name,pack.pages[i].pageColor));
               }
               packData.pagesData.Add(levelCellData);
            }
            string openPath = path + pack.name + ".txt";
            if (File.Exists(openPath))
            {
               // nCompletados (ej: 10)
               // ..... nivel completado por linea (nivel estado nMovimientos)
               //  nivel (1-150) /////// estado (0(perfecto) , 1(completado)) ///// nMovimientos (ej: 6)
               string[] lines = File.ReadAllLines(openPath);
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
                  levelCellData.state = perfect ? LevelData.LevelState.PERFECT : LevelData.LevelState.COMPLETED;
                  packData.pagesData[listPos][gridPos] = levelCellData;
               }
            }
            _packsData.Add(packData);
         }
      }
   }

   public static void LevelPassed(LevelData data)
   {
      foreach (PackData pack in _packsData)
      {
         if (pack.name == data.packName)
         {
            int realPos = data.name - 1;
            int listPos = realPos / 30;
            int gridPos = realPos - listPos * 30;
            var levelCellData = pack.pagesData[listPos][gridPos];
            levelCellData.bestMovements = data.bestMovements;
            levelCellData.state = data.state;
            pack.pagesData[listPos][gridPos] = levelCellData;
            return;
         }
      }
      Debug.LogError("Couldn't find that level");
   }

   public static PackData GetPackData(string name)
   {
      foreach (PackData pack in _packsData)
      {
         if(pack.name == name)
            return pack;
      }
      throw new Exception("No pack found, oh oh");
   }


   public static void SaveCurrentData()
   {
      List<string> dataToSave = new List<string>();
      foreach (PackData pack in _packsData)
      {
         dataToSave.Clear();
         // Counter
         dataToSave.Add("");
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

         if (dataToSave.Count != 1)
         {
            dataToSave[0] = (dataToSave.Count - 1).ToString();
            File.WriteAllLines(path + pack.name + ".txt",dataToSave);
         }
      }
   }
}
