using System;
using System.Collections.Generic;
using System.Text.RegularExpressions;
using UnityEngine;

public class PuzzleParser
{
    public class Puzzle
    {
        private readonly int _size;
        private List<Vector2>[] _flows;
        private int _levelNumber;
        private int _flowCount;

        public Puzzle(int s, int n, int flowCount)
        {
            _size = s;
            _levelNumber = n;
            _flowCount = flowCount;
            _flows = new List<Vector2>[_flowCount];
        }

        public int GetSize()
        {
            return _size;
        }

        public int GetLevelNumber()
        {
            return _levelNumber;
        }

        public int GetFlowCount()
        {
            return _flowCount;
        }

        public void AddFlow(List<Vector2> flow, int index)
        {
            _flows[index] = flow;
        }
        public List<Vector2> GetFlow(int n)
        {
            return _flows[n];
        }
    }

    //TODO Read both width and height and stop assuming squares
    public Puzzle ParsePuzzle(string data)
    {
        Puzzle puzzle = null;

        //Remove all whitespace
        Regex.Replace(data, @"\s+", "");

        //Divide data string by lines
        String[] splits = data.Split(';');

        //Divide first line by parameters
        String[] info = splits[0].Split(',');

        if (!int.TryParse(info[0], out int size))
        {
            return null;
        }

        if (!int.TryParse(info[2], out int levelNumber))
        {
            return null;
        }

        if (!int.TryParse(info[3], out int flowCount))
        {
            return null;
        }

        puzzle = new Puzzle(size, levelNumber, flowCount);

        for (int i = 1; i < splits.Length; i++)
        {
            String[] stringCoords = splits[i].Split(',');
            List<Vector2> list = new List<Vector2>(stringCoords.Length);

            for (int j = 0; j < stringCoords.Length; j++)
            {
                int coord;
                if (!int.TryParse(stringCoords[j], out coord))
                {
                    Debug.LogError($"Malformed coordinate${j}");
                    return null;
                }
                list.Add(new Vector2(coord%size, (int)(coord/size)));
            }

            puzzle.AddFlow(list, i);
        }

        return puzzle;
    }
}