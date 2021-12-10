using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.Serialization;

public class BoardManager : MonoBehaviour
{
    [SerializeField] private new Camera camera;

    [SerializeField] private Cell cellPrefab;

    [SerializeField] private GameObject grid;

    private const int TopBarSize = 1;
    private const int BottomBarSize = 1;

    private Cell[,] _cells;
    private Cell _selectedCell = null;
    private int _selectedCellX = 0;
    private int _selectedCellY = 0;

    // Start is called before the first frame update
    void Start()
    {
        //This wont be done once we have a level and game managers   
        StartLevel(6);
    }

    //Will be a level object later that has a full description of a level 
    private void StartLevel( /*Level level*/ int size)
    {
        _cells = new Cell[size, size];
        camera.orthographicSize = size / 2 + TopBarSize + BottomBarSize;
        for (int i = 0; i < size; i++)
        for (int j = 0; j < size; j++)
        {
            _cells[i, j] = Instantiate(cellPrefab, logicToWorld(new Vector2(i, j)), Quaternion.identity,
                grid.transform);
        }

        _cells[0, 0].SetCircle(Color.red);
        _cells[size - 1, size - 1].SetCircle(Color.green);
    }

    private Vector3 logicToWorld(Vector2 position)
    {
        int size = _cells.GetLength(0);
        float offset = (float) (size % 2 == 1 ? (int) (size / 2) : (int) (size / 2) - 0.5);
        return new Vector3(position.x - offset, position.y - offset, 0);
    }

/*
 *
 *     x - off = nx
 *     nx + off = x
 * 
 */
    private Vector2 worldToLogic(Vector3 position)
    {
        int size = _cells.GetLength(0);
        float offset = (float) (size % 2 == 1 ? (int) (size / 2) : (int) (size / 2) - 0.5);
        return new Vector2((float) Math.Round(position.x + offset), (float) Math.Round(position.y + offset));
    }

    private void Update()
    {
        int size = _cells.GetLength(0);
        /*Lets handle them touch events*/
        if (Input.touchCount > 0)
        {
            Touch touch = Input.GetTouch(0);
            float x = camera.ScreenToWorldPoint(touch.position).x;
            float y = camera.ScreenToWorldPoint(touch.position).y;

            Vector3 trv = worldToLogic(new Vector3(x, y, 0));
            int logicX = (int) trv.x;
            int logicY = (int) trv.y;

            if (logicX >= 0 && logicX < size && logicY >= 0 && logicY < size)
            {
                //TENEMOS UNA CELDA SEÑORES
                if (!_selectedCell && touch.phase == TouchPhase.Began)
                {
                    _selectedCell = _cells[logicX, logicY];
                    _selectedCellX = logicX;
                    _selectedCellY = logicY;
                    _selectedCell.SetCircle(Color.blue);
                }
                else if (_selectedCell)
                {
                    Cell actual = _cells[logicX, logicY];
                    //TENEMOS UNA CELDA SEÑORES
                    if(_selectedCell != actual)
                        actual.SetEnterFlow(Color.blue, new Vector2(logicX-_selectedCellX, logicY-_selectedCellY));
                    if (touch.phase == TouchPhase.Ended || touch.phase == TouchPhase.Canceled )
                    {
                        _selectedCell = null;
                        Debug.Log("AAAAAA, PITOO");                    
                    }
                }
            }
        }
    }
}