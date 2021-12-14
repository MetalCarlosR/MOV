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
    private List<List<Cell>> _previousFlows;
    private int _previousColor = -1;

    private Cell[,] _cells;
    private Cell _selectedCircle = null;
    private List<Cell> _selectedFlow = null;
        
    private PuzzleParser.Puzzle _puzzle;
    private readonly Color[] _colors = {Color.cyan, Color.magenta, Color.green, Color.yellow, Color.grey};
    private int _stepCount = 0;

    //This wont be done here, once we have a game manager he should set level and call startLevel 
    //TODO(Ricky): GameManaer support
    //TODO(Ricky): LevelPack and not string 
    void Start()
    {
        string level = @"5,0,1,5;0,5,10,15,20,21;2,1,6,11,16;7,12,17,22;4,3,8,13,18;9,14,19,24,23;";
        _puzzle = PuzzleParser.ParsePuzzle(level);
        StartLevel();
    }

    //Will be a level object later that has a full description of a level 
    private void StartLevel()
    {
        int width = _puzzle.Width;
        int height = _puzzle.Height;
        _cells = new Cell[width, height];
        camera.orthographicSize = width / 2 + TopBarSize + BottomBarSize;
        
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
            {
                _cells[i, j] = Instantiate(cellPrefab, LogicToWorld(new Vector2(i, j)), Quaternion.identity,
                    grid.transform);
                _cells[i, j].gameObject.name = $"({i},{j})";
                _cells[i, j].SetCoords(i,j);
            }

        _flows = new List<List<Cell>>();
        for (int i = 0; i < _puzzle.FlowCount; i++)
        {
            var flow = _puzzle.GetFlow(i);
            Cell initial = _cells[(int) flow[0].x, (int) flow[0].y];
            initial.SetCircle(_colors[i]);

            Cell final = _cells[(int) flow[flow.Count - 1].x, (int) flow[flow.Count - 1].y];
            final.SetCircle(_colors[i]);

            var path = new List<Cell>();
            _flows.Add(path);
        }
        UpdatePreviousState();
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

    private void ClearFlow(List<Cell> flow, int first)
    {
        int last = flow.Count;

        for (int i = first; i<last;i++)
        {
            flow[i].ResetCell();
        }

        Cell firstCutCell = null;
        if (flow.Count > 1)
            firstCutCell = flow[first];

        flow.RemoveRange(first,last-first);

        // the origin is only remaining
        if (flow.Count == 1)
        {
            flow[0].ResetCell();
        }
        // cut properly the rest of the chain
        else if (flow.Count > 1)
        {
            Cell lastRemainingCell = flow.Last<Cell>();
            Vector2 logicPos = WorldToLogic(lastRemainingCell.transform.position);

            // search the direction the last cell remaining and the first cut one were
            if (logicPos.y - 1 >= 0 && _cells[(int)logicPos.x, (int)logicPos.y - 1] == firstCutCell)
            {
                lastRemainingCell.DisconnectUp();
            }
            else if (logicPos.y + 1 <= _puzzle.Height && _cells[(int)logicPos.x, (int)logicPos.y + 1] == firstCutCell)
            {
                lastRemainingCell.DisconnectDown();
            }
            else if (logicPos.x - 1 >= 0 && _cells[(int)logicPos.x - 1, (int)logicPos.y] == firstCutCell)
            {
                lastRemainingCell.DisconnectLeft();
            }
            else
            {
                lastRemainingCell.DisconnectRight();
            }
        }
    }

    private int GetColorIndexByCell(Cell cell)
    {
        for(int i = 0;i<_colors.Length;i++)
            if (_colors[i] == cell.GetColor())
                return i;
        return -1;
    }
    private List<Cell> GetFlowByCell(Cell cell)
    {
        int i = GetColorIndexByCell(cell);
        return i == -1 ? null : _flows[i];
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
                return;

            Cell actual = _cells[logicX, logicY];
            //Selecting start of flow
            if (!_selectedCircle && touch.phase == TouchPhase.Began)
            {
                if (!actual.IsCircle() && actual.IsInUse())
                {
                    _selectedFlow = GetFlowByCell(actual);
                    _selectedCircle = _selectedFlow.First();
                    // solves a bug that if you disconnect a resolved flow, you can make it larger than you should
                    if(_selectedCircle.IsCircle())
                        _selectedCircle = _selectedFlow.First();
                    _selectedCircle.DespawnMiniCircle();
                }
                else if (actual.IsCircle())
                {
                    //ClearList
                    ClearFlow(GetFlowByCell(actual),0);
                    _selectedCircle = _cells[logicX, logicY];
                    _selectedFlow = GetFlowByCell(_selectedCircle);
                    _selectedFlow.Add(actual);
                }
            }
            else if (_selectedCircle)
            {
                TryToExtendCurrentFlow(actual);

                if (touch.phase == TouchPhase.Ended || touch.phase == TouchPhase.Canceled)
                {
                    BreakFlow();
                }
            }
        }
    }

    /// <summary>
    /// Adds the cell to the flow and returns if the algorithm should keep searching
    /// </summary>
    /// <param name="flow"></param>
    /// <param name="objective"></param>
    /// <param name="actual"></param>
    /// <param name="diagonal">if true, it won't break if it can't go in one direction</param>
    /// <returns> if the algorithm should stop searching (it found the end, actual, or the path was blocked)</returns>
    private bool AddCellToFlowIfPossible(List<Cell> flow, Cell objective, Cell actual, bool diagonal)
    {
        Cell first = flow.First();
        // if its not a circle or is a circle of the flow's color
        if (!actual.IsCircle() || first.GetColor() == actual.GetColor())
        {
            // check if the flow already owns the cell
            // if it does, clear the flow and add that cell
            if (flow.Contains(actual))
                ClearFlow(flow, flow.FindIndex(cell => cell == actual));

            flow.Add(actual);

            // if you encounter either the objective, or the other end of the flow
            if (actual == objective || (actual.IsCircle() && flow.Count > 1 && actual.GetColor() == first.GetColor()))
                // signal that the algorithm is done
                return true;
            return false;
        }
        // if you can't continue, also signal the algorithm to stop, unless its in a perfect diagonal
        else return !diagonal;
    }

    /// <summary>
    /// checks if the end of a flow is only in the middle of a flow, not at the beginning or end
    /// </summary>
    /// <param name="flow"></param>
    /// <returns></returns>
    private bool HasTheEndInMiddleOfFlow(List<Cell> flow)
    {
        for (int i = 1; i< flow.Count-1; i++)
        {
            if (flow[i].IsCircle())
                return true;
        }
        return false;
    }

    /// <summary>
    /// updates the connections between 2 cells
    /// </summary>
    /// <param name="actual"></param>
    /// <param name="prev"></param>
    private void UpdateCellConnections(Cell actual, Cell prev)
    {
        Vector3 dir = actual.transform.position - prev.transform.position;

        bool finishingCircle = actual.IsCircle() && actual.GetColor() == _selectedCircle.GetColor();

        //Connected flow
        // if (!actual.IsCircle() || finishingCircle)
        {
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
        // connects the previous one as well
        if (dir.x != 0)
            if (dir.x == -1)
                prev.ConnectLeft();
            else
                prev.ConnectRight();
        else if (dir.y == -1)
            prev.ConnectDown();
        else
            prev.ConnectUp();
    }

    /// <summary>
    /// builds a path if posible to an objective, and connects it to a flow.
    /// It may eliminate existing cells previously connected to a flow
    /// </summary>
    /// <param name="objective"></param>
    /// <param name="flow"> must be ref because its pointer may change</param>
    /// <returns></returns>
    private bool TryCreatingPathToObjective(Cell objective, ref List<Cell> flow)
    {
        if (flow.Contains(objective))
            return false;

        List<Cell> f = new List<Cell>(flow);

        while (true)
        {
            Vector2 logicPosLast = WorldToLogic(f.Last().transform.position);

            Vector2 dir = WorldToLogic(objective.transform.position) - logicPosLast;

            // if there is more x than y to travel, try that direction only
            if (Mathf.Abs(dir.x) > Mathf.Abs(dir.y))
            {
                Cell actual = _cells[(int)(logicPosLast.x + 1 * (dir.x / Mathf.Abs(dir.x))), (int)logicPosLast.y];
                // if its not able to add it for any reason, stop trying to build a path
                if (AddCellToFlowIfPossible(f, objective, actual, false))
                    break;
            }
            // do the same but for y
            else if (Mathf.Abs(dir.x) < Mathf.Abs(dir.y))
            {
                Cell temp = _cells[(int)logicPosLast.x, (int)(logicPosLast.y + 1 * (dir.y / Mathf.Abs(dir.y)))];
                if (AddCellToFlowIfPossible(f, objective, temp, false))
                    break;
            }
            // perfect diagonal
            else
            {
                // try both x and y
                Debug.Log("Diagonal");
                Cell temp = _cells[(int)(logicPosLast.x + 1 * (dir.x / Mathf.Abs(dir.x))), (int)logicPosLast.y];
                if (AddCellToFlowIfPossible(f, objective, temp, true))
                    break;
                else
                {
                    // just in case the previous diagonal was able to do it
                    logicPosLast = WorldToLogic(f.Last().transform.position);
                    temp = _cells[(int)logicPosLast.x, (int)(logicPosLast.y + 1 * (dir.y / Mathf.Abs(dir.y)))];
                    if (AddCellToFlowIfPossible(f, objective, temp, true))
                        break;
                }
            }
        }

        // if it has it or it reached the end previously
        if (f.Contains(objective) && !HasTheEndInMiddleOfFlow(f))
        {
            flow.Clear();
            foreach (Cell c in f)
            {
                flow.Add(c);
            }
            return true;
        }
        else return false;
    }

    /// <summary>
    /// Try adding a cell to the flow and, if its able to, updates its connections
    /// </summary>
    /// <param name="actual"></param>
    /// <param name="flow"> must be ref because the pointer may change</param>
    /// <returns>if the adding was succesfull or not</returns>
    private bool TryAddingCellToFlow(Cell actual, ref List<Cell> flow)
    {
        if (flow.Contains(actual))
            return false;

        Cell last = flow.Last();

        bool res = TryCreatingPathToObjective(actual, ref flow);

        // updates all conections
        // must do all because previous method doesn't respect original flow, and may alter previous cells
        for (int i = 1; i < flow.Count; i++)
        {
            flow[i].SetColor(_selectedCircle.GetColor());
            UpdateCellConnections(flow[i], flow[i - 1]);
        }
        return res;
    }

    private void TryToExtendCurrentFlow(Cell actual)
    {
        //We collided with different color circle
        if (actual.IsCircle() && actual.GetColor() != _selectedCircle.GetColor())
            return;

        actual.GetCoords(out int x, out int y);
        Debug.Log($"Trying ({x} {y}) Color({actual.GetColor()}) SelectedColor({_selectedCircle.GetColor()})");
        
        //Loop in flow or going back in it
        if (actual.GetColor() == _selectedCircle.GetColor())
        {
            //We ignore it if it's the last
            if (_selectedFlow.Count > 0 &&  (actual != _selectedFlow.Last() || _selectedFlow.First() == _selectedFlow.Last()) 
                                        || (actual.IsCircle() && actual == _selectedCircle))
            {
                Debug.Log($"Loop {_selectedFlow.Count}");
                int index = _selectedFlow.FindIndex(cell => cell == actual);

                List<Cell> CutCells = _selectedFlow.GetRange(index + 1, _selectedFlow.Count-(index + 1));

                if(index >= 0)
                    ClearFlow(_selectedFlow, index + 1);

                foreach (Cell cell in CutCells)
                {
                    TryToRecoverPreviousFlow(cell, GetColorIndexByCell(actual));
                }
            }
        }
        
        var previousFlow = GetFlowByCell(actual);
        if (previousFlow != null && !previousFlow.Contains(actual))
            previousFlow = null;

        //Add it to the selected list if it's not there yet
        if (TryAddingCellToFlow(actual, ref _selectedFlow))
        {
            if (previousFlow != null)
            {
                ClearFlow(previousFlow, previousFlow.FindIndex(cell => cell == actual));

                Cell prev = null;

                foreach (var cell in _selectedFlow.Where(cell => cell != actual))
                    prev = cell;

                if (prev == null)
                {
                    Debug.Log("Prev was null");
                    return;
                }
                
                actual.SetColor(_selectedCircle.GetColor());
                UpdateCellConnections(actual, prev);
            }
        }
    }

    private void TryToRecoverPreviousFlow(Cell actual, int colorIndex)
    {
        actual.GetCoords(out int x, out int y);

        int index = 0;
        bool found = false;
        foreach (var flow in _previousFlows)
        {
            foreach (Cell cell in flow)
            {
                cell.GetCoords(out int ouxX, out int ouxY);
                if (ouxX == x && ouxY == y)
                {
                    found = true;
                    break;
                }
            }
            if (found)
                break;
            index++;
        }
        
        if(!found || index == colorIndex)
            return;
        
        //We expand until we get one cell of the current state with the current color colliding with what we want to
        //restore

        var previousFlow = _previousFlows[index];
        var actualFlow = _flows[index];
        foreach (Cell cell in previousFlow)
        {
            cell.GetCoords(out int ouxX, out int ouxY);
            //Collision, break out
            if (_cells[ouxX, ouxY].GetColor() == _selectedCircle.GetColor())
                break;
            
            //All good, add it in the current state
            Cell cellToAdd = _cells[ouxX, ouxY];
            if (!actualFlow.Contains(cellToAdd))
            {
                cellToAdd.SetColor(_colors[index]);
                cellToAdd.Fill();
                UpdateCellConnections(cellToAdd, actualFlow.Last());
                actualFlow.Add(cellToAdd);
            }
        }
        //We set the rectangle on the ones that came before the one we are adding
        for(int i =0; i<index;i++)
            actualFlow[i].Fill();
    }

    private void BreakFlow()
    {
        if (_selectedFlow == null || _selectedCircle == null)
        {
            Debug.Log("Breaking non existing flow");
            return;
        }
        
        if (_selectedFlow.Count > 1)
        {
            foreach (Cell cell in _selectedFlow)
            {
                cell.Fill();
            }
            _selectedFlow.Last().SpawnMiniCircle();  
        }
        //If we only had one its the circle, clear it and clear the list
        if (_selectedFlow.Count == 1)
        {
            _selectedCircle.ResetCell();
            _selectedFlow.Clear();
        }

        
        bool differ = StatesDiffer();
        
        //If they differ we count one step up, only when it's not the same color we touched last round
        if (differ)
            _stepCount++;

        //Copy the current state to previous when there are changes on any path
        if (differ || _previousColor == GetColorIndexByCell(_selectedCircle))
        {
            //Deep copy 
            UpdatePreviousState();
            _previousColor = GetColorIndexByCell(_selectedCircle);
        }
        _selectedCircle = null;
        _selectedFlow = null;
    }

    private void UpdatePreviousState()
    {
        _previousFlows = new List<List<Cell>>();
        foreach (List<Cell> flow in _flows)
            _previousFlows.Add(new List<Cell>(flow));
    }

    //Returns whether previous state and current differ. With the exception of modifying consecutively the same color.
    //Because we don't count that as steps.
    private bool StatesDiffer()
    {
        //Easy check for early exit
        if (_previousFlows == null)
            return true;

        if (_previousColor != GetColorIndexByCell(_selectedCircle))
            return true;
        
        return false;
        //TODO: with undos, we need more shit here, for now we count
        bool changesOnSameColor = true;
        int i;
        
        //They differ if any path has more flows than before. Unless its the same we were already updating last state
        for (i = 0; i < _flows.Count; i++)
            if (_previousFlows[i].Count != _flows[i].Count)
            {
                int index = GetColorIndexByCell(_selectedCircle);
                //We have changes on color different than the one we hare selecting
                if (i != index)
                    return true;
                changesOnSameColor = _flows[index].Count != _flows[index].Count;
                break;                
            }
        
        //We differ but are touching the same color
        //Let's try to restore the havoc caused by our path without breaking our
        //changes
        if (i < _flows.Count && !changesOnSameColor)
        {
            for (int j = _previousFlows[i].Count-1; j>0;j--)
            {
                if (_selectedFlow.Contains(_previousFlows[i][j]))
                    break;
                //Here we try to restore 
            }
        }

        return changesOnSameColor;
    }

    public void Undo()
    {
        Debug.Log("UNDO");
    }

    public int GetStepCount()
    {
        return _stepCount;
    }
}