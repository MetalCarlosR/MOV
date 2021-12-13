using System.Collections;
using System.Collections.Generic;
using TMPro;
using UnityEngine;

public class PageUI : MonoBehaviour
{
    [SerializeField] private List<LevelCellUI> cells;
    [SerializeField] private TextMeshProUGUI pageName;

    public void SetupPage(string[] levelsData, LevelPack.LevelPage pageData, int startNumber)
    {
        pageName.text = pageData.pageName;
        for(int i = 0; i < cells.Count; i++)
            cells[i].SetupCell(levelsData[i],startNumber+i,pageData.pageColor);
    }
}