﻿using System;
using System.Collections.Generic;
using System.Diagnostics.CodeAnalysis;
using System.Text.RegularExpressions;
using UnityEngine;

[SuppressMessage("ReSharper", "RedundantCast")]
public static class PuzzleParser
{
    public class Puzzle
    {
        public int Width { get; private set; }
        public int Height { get; private set; }
        public int LevelNumber { get; private set; }
        public int FlowCount { get; private set; }
        public List<Vector2> Holes { get; private set; }
        public List<Vector2> Walls { get; private set; }
        public bool Surrounded { get; private set; }

        private readonly List<Vector2>[] _flows;

        public Puzzle(int w,int h, int n, int flowCount, List<Vector2> holes, List<Vector2> walls, bool surrounded)
        {
            Width = w;
            Height = h;
            LevelNumber = n;
            FlowCount = flowCount;
            Holes = holes;
            Walls = walls;
            Surrounded = surrounded;

            _flows = new List<Vector2>[FlowCount];
        }

        public void AddFlow(List<Vector2> flow, int index)
        {
            _flows[index] = flow;
        }

        public List<Vector2> GetFlow(int n)
        {
            return n==-1 || n >= _flows.Length ? null: _flows[n];
        }
    }

    /// <summary>
    /// Parses a puzzle from file to a puzzle type
    /// </summary>
    /// <param name="data"></param>
    /// <returns>The parsed puzzle. If unable, will return null</returns>
    public static Puzzle ParsePuzzle(string data)
    {
        //Remove all whitespace
        Regex.Replace(data, @"\s+", string.Empty);
        
        //Divide data string by lines
        String[] splits = data.Split(';');

        //---------------HEADER---------------
        //Divide first line by parameters
        String[] info = splits[0].Split(',');

        String[] sizes = info[0].Split(':');
        if (!int.TryParse(sizes[0], out int width))
        {
            Debug.LogError("Badly formatted width");
            return null;
        }

        bool surroundMap = false;
        
        int height = width;
        if (sizes.Length == 2)
        {
            String[] heights = sizes[1].Split('+');
            if (heights.Length > 1 && heights[1] == "B")
                surroundMap = true;
            if(!int.TryParse(heights[0], out height))
            {
                Debug.LogError("Badly formatted height (:)");
                return null;
            }
        }

        if (!int.TryParse(info[2], out int levelNumber))
        {
            Debug.LogError("Could not load level number");
            return null;
        }

        if (!int.TryParse(info[3], out int flowCount))
        {
            Debug.LogError("Could not load flow count");
            return null;
        }

        List<Vector2> holes = null;
        //We ignore 5, not required 
        //Holes divided by :
        if (info.Length >= 6 && info[5] != "")
        {
            holes = new List<Vector2>();
            var stringHoles = info[5].Split(':');
            foreach (string hole in stringHoles)
            {
                if (!int.TryParse(hole, out var coord))
                {
                    Debug.LogError($"Malformed hole coordinate");
                    return null;
                }
                holes.Add(GetVecFromCoord(coord, width));
            }
        }
        
        List<Vector2> walls = null;
        //Walls divided by : I guess, who knows haha
        if (info.Length >= 7 && info[6] != "")
        {
            walls = new List<Vector2>();
            var stringWalls = info[6].Split(':');
            foreach (string wall in stringWalls)
            {
                var pair = wall.Split('|');
                if (!int.TryParse(pair[0], out var coordA))
                {
                    Debug.LogError($"Malformed wall A coordinate");
                    return null;
                }
                walls.Add(GetVecFromCoord(coordA, width));
                
                if (!int.TryParse(pair[1], out var coordB))
                {
                    Debug.LogError($"Malformed wall B coordinate");
                    return null;
                }
                walls.Add(GetVecFromCoord(coordB, width));
            }
        }
        
        var puzzle = new Puzzle(width, height, levelNumber, flowCount, holes, walls, surroundMap);

        //---------------FLOWS---------------
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
                list.Add(GetVecFromCoord(coord, width));
            }

            puzzle.AddFlow(list, i);
        }

        return puzzle;
    }

    private static Vector2 GetVecFromCoord(int coord, int width)
    {
        return new Vector2(coord % width, (int) (coord / width));
    }
}