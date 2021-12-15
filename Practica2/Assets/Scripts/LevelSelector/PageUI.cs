using System.Collections.Generic;
using TMPro;
using UnityEngine;

public class PageUI : MonoBehaviour
{
    [SerializeField] private List<LevelCellUI> cells;
    [SerializeField] private TextMeshProUGUI pageName;

    public void SetupPage(string name, List<DataManager.LevelCellData> cellsData, bool locked )
    {
        pageName.text = name;
        for(int i = 0; i < cellsData.Count; i++)
            cells[i].SetupCell(cellsData[i], locked && cellsData[i].name != "1");
    }
}