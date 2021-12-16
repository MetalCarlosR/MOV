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
    [SerializeField] private GridLayoutGroup grid;


    public void SetupPage(string name, List<DataManager.LevelData> cellsData, float tileWidth, bool locked)
    {
        pageName.text = name;
        for (int i = 0; i < cellsData.Count; i++)
        {
            bool lockedCheck = locked && !(cellsData[i].name == 1 ||
                                           (i > 0 && cellsData[i - 1].state !=
                                               DataManager.LevelData.LevelState.UNCOMPLETED));
            cells[i].SetupCell(cellsData[i], lockedCheck);
        }

        grid.spacing = Vector2.one * (tileWidth / 3);
        grid.cellSize = Vector2.one * tileWidth;
    }

}