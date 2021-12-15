using System;
using System.Collections.Generic;
using UnityEngine;
using System.IO;

public static class DataManager
{
   public struct LevelCellData
   {
      public enum CellState
      {
         FREE,
         COMPLETED,
         PERFECT
      }
      
      public string data;
      public string name;
      public Color color;
      public int bestMovements;
      public CellState state;

      public LevelCellData(string data, string name, Color color, int bestMovements = -1,
         CellState state = CellState.FREE)
      {
         this.data = data;
         this.name = name;
         this.color = color;
         this.bestMovements = bestMovements;
         this.state = state;
      }
   }

   public struct PackData
   {
      public List<List<LevelCellData>> pagesData;
      public int completed;
      public string name;

      public PackData(string name)
      {
         pagesData = new List<List<LevelCellData>>();
         this.completed = 0;
         this.name = name;
      }
   }
   
   private static List<PackData> _packDatas = new List<PackData>();

   public static void LoadSaveData(List<PackGroup> groups)
   {
      if (_packDatas.Count != 0)
         return;
      
      foreach (PackGroup group in groups)
      {
         foreach (var pack in group.packs)
         {
            PackData packData = new PackData(pack.name);

            string[] data = pack.file.ToString().Split('\n');
            for (int i = 0; i < pack.pages.Length; i++)
            {
               List<LevelCellData> levelCellData = new List<LevelCellData>();
               for (int j = 0; j < 30; j++)
               {
                  string cellData = data[30 * i + j];
                  levelCellData.Add(new LevelCellData(cellData,((i*30)+ j+1).ToString(),pack.pages[i].pageColor));
               }
               packData.pagesData.Add(levelCellData);
            }
            string path = Application.persistentDataPath + "/" + pack.name + ".txt";
            if (File.Exists(path))
            {
               // nCompletados (ej: 10)
               // ..... nivel completado por linea (nivel estado nMovimientos)
               //  nivel (1-150) /////// estado (0(perfecto) , 1(completado)) ///// nMovimientos (ej: 6)
               string[] lines = File.ReadAllLines(path);
               int nFinished = int.Parse(lines[0]);
               packData.completed = nFinished;
               for (int i = 1; i <= nFinished; i++)
               {
                  string[] line = lines[i].Split(' ');
                  int level = int.Parse(line[0]);
                  bool perfect = line[1] == "0";
                  int movements = int.Parse(line[2]);
                  int listPos = level / 30;
                  int gridPos = level - listPos * 30;
                  var levelCellData = packData.pagesData[listPos][gridPos];
                  levelCellData.bestMovements = movements;
                  levelCellData.state = perfect ? LevelCellData.CellState.PERFECT : LevelCellData.CellState.COMPLETED;
                  packData.pagesData[listPos][gridPos] = levelCellData;
               }
            }
            _packDatas.Add(packData);
         }
      }
   }


   public static PackData GetPackData(string name)
   {
      foreach (PackData data in _packDatas)
      {
         if(data.name == name)
            return data;
      }

      throw new Exception("No pack found, oh oh");
   }
}
