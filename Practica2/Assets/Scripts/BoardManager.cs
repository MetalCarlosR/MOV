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


    private List<List<Cell>> _flows;

    private Cell[,] _cells;
    private Cell _selectedCircle = null;
    private List<Cell> _selectedFlow = null;
        
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

        _flows = new List<List<Cell>>();
        for (int i = 0; i < _puzzle.GetFlowCount(); i++)
        {
            var flow = _puzzle.GetFlow(i);
            Cell initial = _cells[(int) flow[0].x, (int) flow[0].y];
            initial.SetCircle(_colors[i]);

            Cell final = _cells[(int) flow[flow.Count - 1].x, (int) flow[flow.Count - 1].y];
            final.SetCircle(_colors[i]);

            var path = new List<Cell>();
            _flows.Add(path);
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

    private void clearFlow(List<Cell> flow)
    {
        clearFlow(flow, 0, flow.Count);
    }
    private void clearFlow(List<Cell> flow, int first, int last)
    {
        for (int i = first; i<last;i++)
        {
            flow[i].Clear();
        }

        flow.RemoveRange(first,last-first);
    }

    private List<Cell> GetFlowByCell(Cell cell)
    {
        int i = 0;
        foreach (Color color in _colors)
        {
            if (color == cell.GetColor())
                return _flows[i];
            i++;
        }
        return null;
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
            if (logicX < 0 || logicX >= size || logicY < 0 || logicY >= size)
            {
                BreakFlow();
                return;
            }

            Cell actual = _cells[logicX, logicY];
            //Selecting start of flow
            if (!_selectedCircle && touch.phase == TouchPhase.Began)
            {
                List<Cell> flow;
                if (!actual.IsCircle() && actual.IsInUse())
                {
                    _selectedFlow = GetFlowByCell(actual);
                    _selectedCircle = _selectedFlow.Last();
                }
                else if (actual.IsCircle())
                {
                    //ClearList
                    clearFlow(GetFlowByCell(actual));
                    _selectedCircle = _cells[logicX, logicY];
                    _selectedFlow = GetFlowByCell(_selectedCircle);
                    _selectedFlow.Add(actual);
                }
            }
            else if (_selectedCircle)
            {
                if (_selectedCircle != actual)
                    TryToExtendCurrentFlow(actual);

                if (touch.phase == TouchPhase.Ended || touch.phase == TouchPhase.Canceled)
                    BreakFlow();
            }
        }
    }

    private void TryToExtendCurrentFlow(Cell actual)
    {
        //We collided with different color circle
        if (actual.IsCircle() && actual.GetColor() != _selectedCircle.GetColor() && !_selectedFlow.Contains(actual))
        {
            BreakFlow();
            return;
        }
        
        //Add it to the selected list if it's not there yet
        if(!_selectedFlow.Contains(actual))
            _selectedFlow.Add(actual);
        
        //CurrentList
        var flow = GetFlowByCell(actual);

        Cell prev = null;
        
        foreach (var cell in _selectedFlow.Where(cell => cell != actual))
            prev = cell;

        Vector3 dir = actual.transform.position - prev.transform.position;
        
        //Check for loops and other flows

        //Loop in flow
        if(actual.GetColor() == _selectedCircle.GetColor())
        {
            if (actual != _selectedFlow.Last())
            {
                clearFlow(_selectedFlow,_selectedFlow.FindIndex(cell => cell == actual), _selectedFlow.Count);
                return;
            }
        }
        //Cutting other flow?
        else if(flow!=null && flow.Contains(actual) && actual.IsInUse())
        {
            clearFlow(flow,flow.FindIndex(cell => cell == actual), flow.Count);
        }

        bool finishingCircle = actual.IsCircle() && actual.GetColor() == _selectedCircle.GetColor() && !actual.IsInUse();
        
        //Connected flow
        if (!actual.IsCircle() || finishingCircle)
        {
            actual.SetColor(_selectedCircle.GetColor());

            if (dir.x != 0)
                if (dir.x == -1)
                    actual.ConnectRight();
                else
                    actual.ConnectLeft();
            else if (dir.y == -1)
                actual.ConnectUp();
            else
                actual.ConnectDown();
        }
        
        if (dir.x != 0)
            if (dir.x == -1)
                prev.ConnectLeft();
            else
                prev.ConnectRight();
        else if (dir.y == -1)
            prev.ConnectDown();
        else
            prev.ConnectUp();

        //We finished the flow
        if (finishingCircle)
        {
            foreach (Cell cell in _selectedFlow)
            {
                cell.Fill();
                Debug.Log("FILL");
            }
            BreakFlow();
        }
    }

    private void BreakFlow()
    {
        _selectedCircle = null;
        _selectedFlow = null;
    }
}