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
        clearFlow(flow, 0);
    }
    private void clearFlow(List<Cell> flow, int first)
    {
        int last = flow.Count;

        Cell firstCutCell = null;
        if (flow.Count > 1)
             firstCutCell = flow[first];

        for (int i = first; i<last;i++)
        {
            flow[i].Clear();
        }

        flow.RemoveRange(first,last-first);

        // the origin is only remaining
        if (flow.Count == 1)
        {
            flow[0].Clear();
        }
        // cut properly the rest of the chain
        else if (flow.Count > 0)
        {
            Cell lastRemainingCell = flow.Last<Cell>();
            Vector2 logicPos = WorldToLogic(lastRemainingCell.transform.position);
            
            // search the direction the last cell remaining and the first cut one were
            if(logicPos.y - 1 >= 0 && _cells[(int)logicPos.x,(int)logicPos.y-1] == firstCutCell)
            {
                lastRemainingCell.DisconnectUp();
            }
            else if (logicPos.y + 1 <= _puzzle.GetSize() && _cells[(int)logicPos.x, (int)logicPos.y + 1] == firstCutCell)
            {
                lastRemainingCell.DisconnectDown();
            }
            else if (logicPos.x - 1 >= 0 && _cells[(int)logicPos.x-1, (int)logicPos.y] == firstCutCell)
            {
                lastRemainingCell.DisconnectLeft();
            }
            else
            {
                lastRemainingCell.DisconnectRight();
            }
        }
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
                if (!actual.IsCircle() && actual.IsInUse())
                {
                    _selectedFlow = GetFlowByCell(actual);
                    _selectedCircle = _selectedFlow.Last();
                    // solves a bug that if you disconnect a resolved flow, you can make it larger than you should
                    if(_selectedCircle.IsCircle())
                        _selectedCircle = _selectedFlow.First();
                    _selectedCircle.DespawnMiniCircle();
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
                else if(_selectedCircle == _selectedFlow[0] && _selectedFlow.Count > 1)
                {
                    Debug.Log("Update");
                    clearFlow(_selectedFlow, 1);
                }

                if (touch.phase == TouchPhase.Ended || touch.phase == TouchPhase.Canceled)
                {
                    BreakFlow();
                }
            }
        }
    }

    private bool TryAddingCellToFlowRec(Cell actual, Cell last)
    {
        if (_selectedFlow.Contains(actual))
            return false;

        Vector2 logicPosLast = WorldToLogic(last.transform.position);

        Vector2 logicPosNew = WorldToLogic(actual.transform.position);

        Vector2 dir = logicPosNew - logicPosLast;

        Debug.Log(dir);

        // if there is no distance add the cell
        if (dir.magnitude == 0)
        {
            _selectedFlow.Add(last);
            return true;
        }

        //if there is more x than y to travel, try that direction only
        if (Mathf.Abs(dir.x) > Mathf.Abs(dir.y))
        {
            Cell temp = _cells[(int)(logicPosLast.x + 1 * (dir.x / Mathf.Abs(dir.x))), (int)logicPosLast.y];
            if (!temp.IsCircle() || actual.GetColor() == temp.GetColor())
            {
                if (TryAddingCellToFlowRec(actual, temp))
                {
                    if (!_selectedFlow.Contains(last))
                        _selectedFlow.Add(last);
                    return true;
                }
                else return false;
            }
            else return false;
        }
        //do the same but for y
        else if (Mathf.Abs(dir.x) < Mathf.Abs(dir.y))
        {
            Cell temp = _cells[(int)logicPosLast.x, (int)(logicPosLast.y + 1 * (dir.y / Mathf.Abs(dir.y)))];
            if (!temp.IsCircle() || actual.GetColor() == temp.GetColor())
            {
                if(TryAddingCellToFlowRec(actual, temp))
                {
                    if (!_selectedFlow.Contains(last))
                        _selectedFlow.Add(last);
                    return true;
                }
                else return false;
            }
            else return false;
        }
        // perfect diagonal
        else
        {
            // try both
            Debug.Log("Diagonal");
            Cell temp = _cells[(int)(logicPosLast.x + 1 * (dir.x / Mathf.Abs(dir.x))), (int)logicPosLast.y];
            if (!temp.IsCircle() || actual.GetColor() == temp.GetColor())
            {
                if (TryAddingCellToFlowRec(actual, temp))
                {
                    if (!_selectedFlow.Contains(last))
                        _selectedFlow.Add(last);
                    return true;
                }
                return false;
            }
            else
            {
                temp = _cells[(int)logicPosLast.x, (int)(logicPosLast.y + 1 * (dir.y / Mathf.Abs(dir.y)))];
                if (!temp.IsCircle() || actual.GetColor() == temp.GetColor())
                    if(TryAddingCellToFlowRec(actual, temp))
                    {
                        if (!_selectedFlow.Contains(last))
                            _selectedFlow.Add(last);
                        return true;
                    }

                return false;
            }
        }
    }

    // TODO: se pueden hacer diagonales y rompertelo, hay que mejorar el algoritmo (las diagonales del original pueden ser de mas de 1 bloque)
    private bool TryAddingCellToFlow(Cell actual)
    {
        if (_selectedFlow.Contains(actual))
            return false;

        Cell last = _selectedFlow.Last();

        // TODO: diagonales... 
        //return TryAddingCellToFlowRec(actual, last);

        Vector2 logicPosLast = WorldToLogic(last.transform.position);

        if ((logicPosLast.y - 1 >= 0 && _cells[(int)logicPosLast.x, (int)logicPosLast.y - 1] == actual)
            || (logicPosLast.y + 1 <= _puzzle.GetSize() && _cells[(int)logicPosLast.x, (int)logicPosLast.y + 1] == actual)
            || (logicPosLast.x - 1 >= 0 && _cells[(int)logicPosLast.x - 1, (int)logicPosLast.y] == actual)
            || (logicPosLast.x + 1 <= _puzzle.GetSize() && _cells[(int)logicPosLast.x + 1, (int)logicPosLast.y] == actual))
        {
            Debug.Log("Added");
            _selectedFlow.Add(actual);
            return true;
        }
        else
        {
            return false;
        }
    }

    private void TryToExtendCurrentFlow(Cell actual)
    {
        //We collided with different color circle
        if (actual.IsCircle() && actual.GetColor() != _selectedCircle.GetColor() && !_selectedFlow.Contains(actual))
        {
            return;
        }

        //Loop in flow or going back in it
        if (actual.GetColor() == _selectedCircle.GetColor())
        {
            if (actual != _selectedFlow.Last())
            {
                Debug.Log("Loop");
                int index = _selectedFlow.FindIndex(cell => cell == actual);
                if(index >= 0)
                    clearFlow(_selectedFlow, index);
            }
        }

        //Add it to the selected list if it's not there yet
        if (TryAddingCellToFlow(actual))
        {
            //CurrentList
            var flow = GetFlowByCell(actual);

            //Cutting other flow?
            // TODO: tiene que hacerlo solo al final, porque si lo descortas mientras arrastras, recuperas ese flow como estaba antes
            if (flow != null && actual.GetColor() != _selectedCircle.GetColor() && flow.Contains(actual) && actual.IsInUse())
            {
                int index = _selectedFlow.FindIndex(cell => cell == actual);
                if (index >= 0)
                    clearFlow(_selectedFlow, index);
            }

            bool finishingCircle = actual.IsCircle() && actual.GetColor() == _selectedCircle.GetColor() && !actual.IsInUse();

            Cell prev = null;

            foreach (var cell in _selectedFlow.Where(cell => cell != actual))
                prev = cell;

            if (prev == null)
            {
                Debug.Log("Prev was null");
                return;
            }

            Vector3 dir = actual.transform.position - prev.transform.position;

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
                // TODO: no hace esto...
                BreakFlow();
            }
        }
    }

    private void BreakFlow()
    {
        if(_selectedFlow == null || _selectedCircle == null)
        {
            Debug.Log("Breaking non existing flow");
            return;
        }
        foreach (Cell cell in _selectedFlow)
        {
            cell.Fill();
        }
        _selectedFlow.Last<Cell>().SpawnMiniCircle();
        _selectedCircle = null;
        _selectedFlow = null;
    }
}