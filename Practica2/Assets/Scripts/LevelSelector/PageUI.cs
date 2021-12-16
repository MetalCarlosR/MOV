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


    public void SetupPage(string name, List<DataManager.LevelData> cellsData, float tileWidth, float targetWidth, bool locked)
    {
        pageName.text = name;
        for (int i = 0; i < cellsData.Count; i++)
        {
            bool lockedCheck = locked && !(cellsData[i].name == 1 ||
                                           (i > 0 && cellsData[i - 1].state !=
                                               DataManager.LevelData.LevelState.UNCOMPLETED));
            cells[i].SetupCell(cellsData[i], lockedCheck);
        }

        float textLeft = ((targetWidth - ((5 + 4f / 3) * tileWidth)) * 0.85f) / 2;
        pageNameParent.offsetMin = new Vector2(textLeft, 0);

        grid.spacing = Vector2.one * (tileWidth / 3);
        grid.cellSize = Vector2.one * tileWidth;
    }

}