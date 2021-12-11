using System;
using System.Collections.Generic;
using System.Diagnostics.CodeAnalysis;
using System.Text.RegularExpressions;
using UnityEngine;

[SuppressMessage("ReSharper", "RedundantCast")]
public static class PuzzleParser
{
    public class Puzzle
    {
        private readonly int _size;
        private readonly List<Vector2>[] _flows;
        private readonly int _levelNumber;
        private readonly int _flowCount;

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
    public static Puzzle ParsePuzzle(string data)
    {
        //Remove all whitespace
        // Regex.Replace(data, @"(\r\n|\n|\r|\s)+", "");
        Regex.Replace(data, @"\s+", string.Empty);
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

        var puzzle = new Puzzle(size, levelNumber, flowCount);

        for (int i = 0; i < flowCount; i++)
        {
            String[] stringCoords = splits[i+1].Split(',');
            List<Vector2> list = new List<Vector2>(stringCoords.Length);

            for (int j = 0; j < stringCoords.Length; j++)
            {
                if (!int.TryParse(stringCoords[j], out var coord))
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