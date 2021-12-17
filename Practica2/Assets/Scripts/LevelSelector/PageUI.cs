using System;
using System.Collections;
using System.Collections.Generic;
using TMPro;
using UnityEngine;
using UnityEngine.UI;

public class PageUI : MonoBehaviour
{
    [SerializeField] private List<LevelCellUI> cells;
    [SerializeField] private TextMeshProUGUI pageName;
    [SerializeField] private RectTransform pageNameParent;
    [SerializeField] private GridLayoutGroup grid;

    /// <summary>
    /// Setup the page with the data from the pack. Calls all the cells inside to
    /// pass them the info with the level associated.
    /// Also scales the page so it fits inside the window.
    /// </summary>
    /// <param name="name">Name of the page</param>
    /// <param name="cellsData">List with the data for the cells</param>
    /// <param name="tileWidth">Width for the tiles inside the grid</param>
    /// <param name="targetWidth">Maximum theoretical width for the grid to cover </param>
    /// <param name="locked">Pack with locked levels </param>
    public void SetupPage(string name, List<DataManager.LevelData> cellsData, float tileWidth, float targetWidth, bool locked)
    {
        pageName.text = name;
        // Pass the data to all the cells
        for (int i = 0; i < cellsData.Count; i++)
        {
            bool lockedCheck = locked && !(cellsData[i].name == 1 ||
                                           (i > 0 && cellsData[i - 1].state !=
                                               DataManager.LevelData.LevelState.UNCOMPLETED));
            cells[i].SetupCell(cellsData[i], lockedCheck);
        }

        // Scales the grid so it fits the window
        float textLeft = ((targetWidth - ((5 + 4f / 3) * tileWidth)) * 0.85f) / 2;
        pageNameParent.offsetMin = new Vector2(textLeft, 0);

        grid.spacing = Vector2.one * (tileWidth / 3);
        grid.cellSize = Vector2.one * tileWidth;
    }

}