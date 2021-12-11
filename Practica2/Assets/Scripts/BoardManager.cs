using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using UnityEngine;
using UnityEngine.Serialization;

public class BoardManager : MonoBehaviour
{
    [SerializeField] private new Camera camera;

    [SerializeField] private Cell cellPrefab;

    [SerializeField] private GameObject grid;

    private const int TopBarSize = 1;
    private const int BottomBarSize = 1;


    private Dictionary<Cell, List<Cell>> _flows;

    private Cell[,] _cells;
    private Cell _selectedCircle = null;

    private PuzzleParser.Puzzle _puzzle;
    private readonly Color[] _colors = {Color.cyan, Color.magenta, Color.green, Color.yellow, Color.grey};

    // Start is called before the first frame update
    void Start()
    {
        string level = @"5,0,1,5;0,5,10,15,20,21;2,1,6,11,16;7,12,17,22;4,3,8,13,18;9,14,19,24,23;";
        _puzzle = PuzzleParser.ParsePuzzle(level);
        //This wont be done once we have a level and game managers   
        StartLevel();
    }

    //Will be a level object later that has a full description of a level 
    private void StartLevel()
    {
        int size = _puzzle.GetSize();
        _cells = new Cell[size, size];
        camera.orthographicSize = size / 2 + TopBarSize + BottomBarSize;
        for (int i = 0; i < size; i++)
        for (int j = 0; j < size; j++)
        {
            _cells[i, j] = Instantiate(cellPrefab, LogicToWorld(new Vector2(i, j)), Quaternion.identity,
                grid.transform);
            _cells[i, j].gameObject.name = $"({i},{j})";
        }

        _flows = new Dictionary<Cell, List<Cell>>();
        for (int i = 0; i < _puzzle.GetFlowCount(); i++)
        {
            var flow = _puzzle.GetFlow(i);
            Cell initial = _cells[(int) flow[0].x, (int) flow[0].y];
            initial.SetCircle(_colors[i]);

            Cell final = _cells[(int) flow[flow.Count - 1].x, (int) flow[flow.Count - 1].y];
            final.SetCircle(_colors[i]);

            var path = new List<Cell>();
            _flows[final] = path;
            _flows[initial] = path;
        }
    }

    private Vector3 LogicToWorld(Vector2 position)
    {
        int size = _cells.GetLength(0);
        float offset = (float) (size % 2 == 1 ? (int) (size / 2) : (int) (size / 2) - 0.5);
        return new Vector3(position.x - offset, offset - position.y, 0);
    }

    private Vector2 WorldToLogic(Vector3 position)
    {
        int size = _cells.GetLength(0);
        float offset = (float) (size % 2 == 1 ? (int) (size / 2) : (int) (size / 2) - 0.5);
        return new Vector2((float) Math.Round(position.x + offset), (float) Math.Round(offset - position.y));
    }

    private void Update()
    {
        int size = _cells.GetLength(0);
        if (Input.touchCount > 0)
        {
            Touch touch = Input.GetTouch(0);
            float x = camera.ScreenToWorldPoint(touch.position).x;
            float y = camera.ScreenToWorldPoint(touch.position).y;

            Vector3 trv = WorldToLogic(new Vector3(x, y, 0));
            int logicX = (int) trv.x;
            int logicY = (int) trv.y;

            //If we click outside the grid we break out
            if (logicX < 0 || logicX >= size || logicY < 0 || logicY >= size) return;

            Cell actual = _cells[logicX, logicY];
            //Selecting start of flow
            if (!_selectedCircle && touch.phase == TouchPhase.Began)
            {
                //No circle, we can't start here
                if (_flows[actual] == null)
                {
                    foreach (var pair in _flows)
                        if (pair.Value.Find(cell => actual == cell))
                            _selectedCircle = pair.Value[0];

                    return;
                }

                if (_flows[actual].Count == 0 || _flows[actual].First() != actual)
                {
                    _flows[actual].Clear();
                    _flows[actual].Add(actual);
                }

                _selectedCircle = _cells[logicX, logicY];
            }
            else if (_selectedCircle)
            {
                if (_selectedCircle != actual)
                {
                    _flows[_selectedCircle].Add(actual);
                    Cell prev = null;
                    foreach (Cell cell in _flows[_selectedCircle])
                    {
                        if(cell != actual)
                            prev = cell;
                    }
                    
                    Vector3 dir = actual.transform.position - prev.transform.position;
                    if (!actual.IsCircle())
                    {
                        if(dir.x != 0)
                            if(dir.x == -1)
                                actual.ConnectRight();
                            else
                                actual.ConnectLeft();
                        else
                            if(dir.y == -1)
                                actual.ConnectUp();
                            else
                                actual.ConnectDown();
                    }
                    
                    if(dir.x != 0)
                        if(dir.x == -1)
                            prev.ConnectLeft();
                        else
                            prev.ConnectRight();
                    else
                        if(dir.y == -1)
                            prev.ConnectDown();
                        else
                            prev.ConnectUp();
                }

                if (touch.phase == TouchPhase.Ended || touch.phase == TouchPhase.Canceled)
                {
                    _selectedCircle = null;
                }
            }
        }
    }
}