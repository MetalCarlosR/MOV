using System.Collections.Generic;
using TMPro;
using UnityEngine;

public class PageUI : MonoBehaviour
{
    [SerializeField] private List<LevelCellUI> cells;
    [SerializeField] private TextMeshProUGUI pageName;

    public void SetupPage(string name, List<DataManager.LevelData> cellsData, bool locked)
    {
        pageName.text = name;
        for (int i = 0; i < cellsData.Count; i++)
        {
            bool lockedCheck = locked && !(cellsData[i].name == 1 ||
                                          (i > 0 && cellsData[i - 1].state !=
                                              DataManager.LevelData.LevelState.UNCOMPLETED));
            cells[i].SetupCell(cellsData[i], lockedCheck);
        }
    }
}