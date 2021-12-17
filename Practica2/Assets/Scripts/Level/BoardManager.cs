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

    // TODO(Nico): esto se tiene que ir al GameManager
    [SerializeField] private Skin currentSkin;

    private const int TopBarSize = 1;
    private const int BottomBarSize = 1;


    private List<List<Cell>> _flows;
    private List<List<Cell>> _previousFlows;
    private int _previousColor = -1;

    private Cell[,] _cells;
    private Cell _selectedCircle = null;
    private List<Cell> _selectedFlow = null;

    private PuzzleParser.Puzzle _puzzle;
    private /*readonly*/ Color[] _colors;
    private int _completedFlows = 0;
    private int _stepCount = 0;
    private float _progress = 0;

    [SerializeField] private LevelManager levelManager;

    private List<int> _clues;

    private bool _handleInput = true;
    
    public void SetupLevel(PuzzleParser.Puzzle level)
    {
        _colors = currentSkin.colors;
        _puzzle = level;
        if (_puzzle != null)
        {
            StartLevel();
        }
        else
        {
            Debug.LogError("ERROR: Couldn't load map properly!");
            GameManager.Instance.LoadScene(1);
        }
    }

    //Will be a level object later that has a full description of a level 
    private void StartLevel()
    {
        int width = _puzzle.Width;
        int height = _puzzle.Height;
        _cells = new Cell[width, height];
        _clues = new List<int>();
        
        camera.orthographicSize = height / 2 + (TopBarSize + BottomBarSize);

        if (camera.aspect * (camera.orthographicSize) < width / 2)
        {
            camera.orthographicSize = (width / (camera.aspect * 2));
        }

        for (int i = 0; i < width; i++)
        for (int j = 0; j < height; j++)
        {
            _cells[i, j] = Instantiate(cellPrefab, LogicToWorld(new Vector2(i, j)), Quaternion.identity,
                grid.transform);
            _cells[i, j].gameObject.name = $"({i},{j})";
            _cells[i, j].SetCoords(i, j);
        }

        // TODO(Ricky): Incluir ads en el cómputo chachi de escala y coloca los textos como en el juego

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

        // adds holes to the map
        List<Vector2> holes = _puzzle.Holes;
        if (holes != null)
            for (int i = 0; i < holes.Count; i++)
            {
                _cells[(int) holes[i].x, (int) holes[i].y].SetAsHole();
            }

        // adds walls to the map
        List<Vector2> walls = _puzzle.Walls;
        if (walls != null)
            for (int i = 0; i < walls.Count; i += 2) // is +2 cause the vector stores pairs of cells to add walls to
            {
                Vector2 first = walls[i];
                Vector2 second = walls[i + 1];

                // first is to the left
                if (first.x < second.x)
                {
                    _cells[(int) first.x, (int) first.y].WallRight();
                    _cells[(int) second.x, (int) second.y].WallLeft();
                }
                // first is to the right
                else if (first.x < second.x)
                {
                    _cells[(int) first.x, (int) first.y].WallLeft();
                    _cells[(int) second.x, (int) second.y].WallRight();
                }

                // first is above 
                if (first.y < second.y)
                {
                    _cells[(int) first.x, (int) first.y].WallDown();
                    _cells[(int) second.x, (int) second.y].WallUp();
                }
                // first is below
                else if (first.y < second.y)
                {
                    _cells[(int) first.x, (int) first.y].WallUp();
                    _cells[(int) second.x, (int) second.y].WallDown();
                }
                else Debug.LogError("ERROR: Trying to wall to itself");
            }

        // if its surrounded, add the walls to board
        if (_puzzle.Surrounded)
        {
            // in the first and last row
            for (int i = 0; i < width; i++)
            {
                Cell c = _cells[i, 0];
                if (!c.IsHole())
                    c.WallUp();
                c = _cells[i, height - 1];
                if (!c.IsHole())
                    c.WallDown();
            }

            // in the first and last column
            for (int i = 0; i < height; i++)
            {
                Cell c = _cells[0, i];
                if (!c.IsHole())
                    c.WallLeft();
                c = _cells[width - 1, i];
                if (!c.IsHole())
                    c.WallRight();
            }

            // and if there is holes
            // to every hole connected to a non hole board cell
            if (holes != null)
                foreach (Vector2 h in holes)
                {
                    Cell holeCell = _cells[(int) h.x, (int) h.y];
                    Cell c = null;
                    if (h.y > 0)
                    {
                        c = _cells[(int) h.x, (int) h.y - 1];
                        if (!c.IsHole())
                        {
                            holeCell.WallUp();
                            c.WallDown();
                        }
                    }

                    if (h.y < height - 1)
                    {
                        c = _cells[(int) h.x, (int) h.y + 1];
                        if (!c.IsHole())
                        {
                            holeCell.WallDown();
                            c.WallUp();
                        }
                    }

                    if (h.x > 0)
                    {
                        c = _cells[(int) h.x - 1, (int) h.y];
                        if (!c.IsHole())
                        {
                            holeCell.WallLeft();
                            c.WallRight();
                        }
                    }

                    if (h.x < width - 1)
                    {
                        c = _cells[(int) h.x + 1, (int) h.y];
                        if (!c.IsHole())
                        {
                            holeCell.WallRight();
                            c.WallLeft();
                        }
                    }
                }
        }

        UpdatePreviousState();
    }

    private Vector3 LogicToWorld(Vector2 position)
    {
        int size = _cells.GetLength(0);
        float offsetX = (float) (size % 2 == 1 ? (int) (size / 2) : (int) (size / 2) - 0.5);

        size = _cells.GetLength(1);
        float offsetY = (float) (size % 2 == 1 ? (int) (size / 2) : (int) (size / 2) - 0.5);

        return new Vector3(position.x - offsetX, offsetY - position.y, 0);
    }

    private Vector2 WorldToLogic(Vector3 position)
    {
        int size = _cells.GetLength(0);
        float offsetX = (float) (size % 2 == 1 ? (int) (size / 2) : (int) (size / 2) - 0.5);

        size = _cells.GetLength(1);
        float offsetY = (float) (size % 2 == 1 ? (int) (size / 2) : (int) (size / 2) - 0.5);
        return new Vector2((float) Math.Round(position.x + offsetX), (float) Math.Round(offsetY - position.y));
    }

    private void ClearFlow(List<Cell> flow, int first, bool withBackground)
    {
        int last = flow.Count;

        for (int i = first; i < last; i++)
        {
            if (withBackground)
                flow[i].ResetCell();
            else flow[i].ResetCellNoBackground();
        }

        Cell firstCutCell = null;
        if (flow.Count > 1)
            firstCutCell = flow[first];

        flow.RemoveRange(first, last - first);

        // the origin is only remaining
        if (flow.Count == 1)
        {
            if (withBackground)
                flow[0].ResetCell();
            else flow[0].ResetCellNoBackground();
        }
        // cut properly the rest of the chain
        else if (flow.Count > 1)
        {
            Cell lastRemainingCell = flow.Last<Cell>();
            Vector2 logicPos = WorldToLogic(lastRemainingCell.transform.position);

            // search the direction the last cell remaining and the first cut one were
            if (logicPos.y - 1 >= 0 && _cells[(int) logicPos.x, (int) logicPos.y - 1] == firstCutCell)
            {
                lastRemainingCell.DisconnectUp();
            }
            else if (logicPos.y + 1 < _puzzle.Height && _cells[(int) logicPos.x, (int) logicPos.y + 1] == firstCutCell)
            {
                lastRemainingCell.DisconnectDown();
            }
            else if (logicPos.x - 1 >= 0 && _cells[(int) logicPos.x - 1, (int) logicPos.y] == firstCutCell)
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
        for (int i = 0; i < _colors.Length; i++)
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
        if (_handleInput && Input.touchCount > 0 && _cells != null)
        {
            int width = _cells.GetLength(0);
            int height = _cells.GetLength(1);

            Touch touch = Input.GetTouch(0);
            float x = camera.ScreenToWorldPoint(touch.position).x;
            float y = camera.ScreenToWorldPoint(touch.position).y;

            Vector3 trv = WorldToLogic(new Vector3(x, y, 0));
            int logicX = (int) trv.x;
            int logicY = (int) trv.y;

            //If we click outside the grid we break out
            if (logicX < 0 || logicX >= width || logicY < 0 || logicY >= height)
                return;

            Cell actual = _cells[logicX, logicY];
            //Selecting start of flow
            if (_selectedCircle == null && touch.phase == TouchPhase.Began)
            {
                if (!actual.IsCircle() && actual.IsInUse())
                {
                    _selectedFlow = GetFlowByCell(actual);
                    _selectedCircle = _selectedFlow.First();
                    // solves a bug that if you disconnect a resolved flow, you can make it larger than you should
                    if (_selectedCircle.IsCircle())
                        _selectedCircle = _selectedFlow.First();
                    actual.DespawnMiniCircle();
                    foreach (Cell cell in _selectedFlow)
                        cell.Empty();
                }
                else if (actual.IsCircle())
                {
                    //ClearList
                    ClearFlow(GetFlowByCell(actual), 0, true);
                    _selectedCircle = _cells[logicX, logicY];
                    _selectedFlow = GetFlowByCell(_selectedCircle);
                    _selectedFlow.Add(actual);
                }
                else
                    return;
                
                bool differ = StatesDiffer();

                //If they differ we count one step up, only when it's not the same color we touched last round
                if (differ)
                {
                    _stepCount++;
                    updateUITexts();
                    levelManager.EnableUndo();
                }

                //Copy the current state to previous when there are changes on any path
                if (differ || _previousColor == GetColorIndexByCell(_selectedCircle))
                {
                    //Deep copy 
                    UpdatePreviousState();
                    _previousColor = GetColorIndexByCell(_selectedCircle);
                }
            }
            else if (_selectedCircle)
            {
                TryToExtendCurrentFlow(actual);

                if (touch.phase == TouchPhase.Ended || touch.phase == TouchPhase.Canceled)
                {
                    FinishFlow();
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
    private bool AddCellToFlowIfPossible(List<Cell> flow, Cell objective, Cell actual, Vector2 dir, bool diagonal)
    {
        Cell first = flow.First();
        bool[] walls = actual.getWalls();
        int index = 0;
        if (dir.x > 0)
            index = (int) Cell.WallsDirs.left;
        else if (dir.x < 0)
            index = (int) Cell.WallsDirs.right;
        else if (dir.y > 0)
            index = (int) Cell.WallsDirs.up;
        else index = (int) Cell.WallsDirs.down;
        // if its not a circle or is a circle of the flow's color
        // and if its not a hole or a wall coming next
        if ((!actual.IsCircle() || first.GetColor() == actual.GetColor())
            && !actual.IsHole() && !walls[index])
        {
            // check if the flow already owns the cell
            // if it does, clear the flow and add that cell
            if (flow.Contains(actual))
                ClearFlow(flow, flow.FindIndex(cell => cell == actual), false);

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
        for (int i = 1; i < flow.Count - 1; i++)
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

        //Connected flow
        if (dir.x != 0)
            if (dir.x == -1)
                actual.ConnectRight();
            else
                actual.ConnectLeft();
        else if (dir.y == -1)
            actual.ConnectUp();
        else
            actual.ConnectDown();

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
                Vector2 d = new Vector2(dir.x / Mathf.Abs(dir.x), 0f);
                Cell actual = _cells[(int) (logicPosLast.x + 1 * d.x), (int) logicPosLast.y];
                // if its not able to add it for any reason, stop trying to build a path
                if (AddCellToFlowIfPossible(f, objective, actual, d, false))
                    break;
            }
            // do the same but for y
            else if (Mathf.Abs(dir.x) < Mathf.Abs(dir.y))
            {
                Vector2 d = new Vector2(0f, dir.y / Mathf.Abs(dir.y));
                Cell temp = _cells[(int) logicPosLast.x, (int) (logicPosLast.y + 1 * d.y)];
                if (AddCellToFlowIfPossible(f, objective, temp, d, false))
                    break;
            }
            // perfect diagonal
            else
            {
                // try both x and y
                Vector2 d = new Vector2(dir.x / Mathf.Abs(dir.x), 0f);
                Cell temp = _cells[(int) (logicPosLast.x + 1 * (dir.x / Mathf.Abs(dir.x))), (int) logicPosLast.y];
                if (AddCellToFlowIfPossible(f, objective, temp, d, true))
                    break;
                else
                {
                    // just in case the previous diagonal was able to do it
                    logicPosLast = WorldToLogic(f.Last().transform.position);
                    d = new Vector2(0f, dir.y / Mathf.Abs(dir.y));
                    temp = _cells[(int) logicPosLast.x, (int) (logicPosLast.y + 1 * (dir.y / Mathf.Abs(dir.y)))];
                    if (AddCellToFlowIfPossible(f, objective, temp, d, true))
                        break;
                }
            }
        }

        // if it has it or it reached the end previously
        if (f.Contains(objective) && !HasTheEndInMiddleOfFlow(f))
        {
            // deeps copy the flow into it
            flow.Clear();
            foreach (Cell c in f)
            {
                var previousFlow = GetFlowByCell(c);
                if (previousFlow != null && !previousFlow.Contains(c))
                    previousFlow = null;
                HandleCollisionWithOtherFlow(previousFlow, c);
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

        //Loop in flow or going back in it
        if (actual.GetColor() == _selectedCircle.GetColor())
        {
            //We ignore it if it's the last
            if (_selectedFlow.Count > 0 &&
                (actual != _selectedFlow.Last() || _selectedFlow.First() == _selectedFlow.Last())
                || (actual.IsCircle() && actual == _selectedCircle))
            {
                int index = _selectedFlow.FindIndex(cell => cell == actual);

                List<Cell> CutCells = _selectedFlow.GetRange(index + 1, _selectedFlow.Count - (index + 1));

                if (index >= 0)
                    ClearFlow(_selectedFlow, index + 1, false);

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
        TryAddingCellToFlow(actual, ref _selectedFlow);
    }

    private void HandleCollisionWithOtherFlow(List<Cell> previousFlow, Cell actual)
    {
        if (previousFlow != null)
        {
            ClearFlow(previousFlow, previousFlow.FindIndex(cell => cell == actual), false);

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

        if (!found || index == colorIndex)
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
                //cellToAdd.Fill();
                UpdateCellConnections(cellToAdd, actualFlow.Last());
                actualFlow.Add(cellToAdd);
            }
        }
    }

    private void FinishFlow()
    {
        if (_selectedFlow == null || _selectedCircle == null)
        {
            Debug.Log("Breaking non existing flow");
            return;
        }

        // if the flow has been cut in the creation of the new one, clear the backgrounds
        for (int i = 0; i < _flows.Count; i++)
        {
            for (int j = _flows[i].Count; j < _previousFlows[i].Count; j++)
            {
                _previousFlows[i][j].Empty();
            }
        }

        if (_selectedFlow.Count > 1)
        {
            foreach (Cell cell in _selectedFlow)
            {
                cell.Fill();
            }

            _selectedFlow.Last().SpawnMiniCircle();
        }

        int flowIndex = GetColorIndexByCell(_selectedFlow.First());
        
        //If we only had one its the circle, clear it and clear the list
        if (_selectedFlow.Count == 1)
        {
            _selectedCircle.ResetCell();
            _selectedFlow.Clear();
        }
        //We have a finished flow for whom a clue was given
        else if (_selectedFlow.Count > 1 && _selectedFlow.First().IsCircle() && _selectedFlow.Last().IsCircle() && 
                _clues.Contains(flowIndex) && !FlowAndAnswerDiffer(flowIndex) )
        {
            _selectedFlow.First().ShowStar();
            _selectedFlow.Last().ShowStar();
        }

        if (GetFirstWrongFlow() == -1)
        {
            _handleInput = false;
            GameManager.Instance.LevelFinished(_stepCount == _flows.Count, _stepCount);
            levelManager.GameFinished(_stepCount == _flows.Count, _stepCount);
        }

        _progress = 0;
        _completedFlows = 0;
        for(int i = 0; i < _flows.Count; i++)
        {
            _progress += _flows[i].Count;

            if (_flows[i].Count > 1 && _flows[i].Last().IsCircle())
                _completedFlows++;
        }

        int total = _puzzle.Width * _puzzle.Height;

        if (_puzzle.Holes != null)
            total -= _puzzle.Holes.Count;

        _progress = (_progress / total) * 100;

        updateUITexts();

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
    }

    public void Undo()
    {
        foreach (var flow in _flows)
        {
            foreach (Cell cell in flow)
            {
                cell.ResetCell();
            }
        }

        _flows = new List<List<Cell>>();
        foreach (List<Cell> flow in _previousFlows)
            _flows.Add(new List<Cell>(flow));

        foreach (var flow in _flows)
        {
            for (int i = 1; i < flow.Count; i++)
            {
                flow[i].SetColor(flow[0].GetColor());
                UpdateCellConnections(flow[i], flow[i - 1]);
                flow[i - 1].Fill();
            }

            if (flow.Count > 0)
                flow.Last().Fill();
        }
        
        _stepCount--;
        updateUITexts();
    }

    //Returns whether there are clues remaining or not 
    public bool UseClue()
    {
        if (_clues.Count == _puzzle.FlowCount)
            return false;
        
        int index = GetFirstWrongFlow();
        if (index == -1)
        {
            Debug.LogError("Something really wrong went with clue generation");
            return false;
        }

        //Apply First Wrong Flow
        
        Debug.Log($"Correcting{index}");
        _clues.Add(index);
        ApplySolution(index);
        
        return true;
    }

    private int GetFirstWrongFlow()
    {
        for (int i = 0; i < _flows.Count; i++)
        {
            if (FlowAndAnswerDiffer(i) && !_clues.Contains(i))
                return i;
        }

        return -1;
    }

    private bool FlowAndAnswerDiffer(int index)
    {
        var flow = _flows[index];
        var solved = _puzzle.GetFlow(index);

        if (flow.Count != solved.Count)
            return true;
        
        int x, y;

        if (flow.Count > 0)
        {
            flow.First().GetCoords(out x, out y);
            if (!((int) (solved[0].x) == x && (int) (solved[0].y) == y))
                solved.Reverse();
        }

        int i = 0;
        foreach (Cell cell in flow)
        {
            cell.GetCoords(out x, out  y);
            if (!((int) (solved[i].x) == x && (int) (solved[i].y) == y))
                return true;
            i++;
        }
        
        return false;
    }
    
    private void ApplySolution(int index)
    {
        var sol = _puzzle.GetFlow(index);
        int i = 0;
        
        var flow = _flows[index];
        ClearFlow(flow, 0, false);

        foreach (Vector2 coord in sol)
        {
            flow.Add(_cells[(int)coord.x,(int)coord.y]);
            i++;
        }
        for (i = 1; i < flow.Count; i++)
        {
            flow[i].SetColor(flow[0].GetColor());
            UpdateCellConnections(flow[i], flow[i - 1]);
            flow[i - 1].Fill();
        }

        if (flow.Count > 0)
            flow.Last().Fill();

        flow.First().ShowStar();
        flow.Last().ShowStar();
    }

    private void updateUITexts()
    {
        levelManager.SetConnectedFlowsText(_completedFlows);
        levelManager.SetStepsText(_stepCount);
        levelManager.SetProgressText((int)_progress);
    }
}