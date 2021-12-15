using System;
using System.Collections;
using System.Collections.Generic;
using TMPro;
using UnityEngine;
using UnityEngine.UI;

public class LevelCellUI : MonoBehaviour
{
    private DataManager.LevelData _levelData;
    private bool _locked;
    [SerializeField] private TextMeshProUGUI lvlNumber;
    [SerializeField] private Image border, perfectUI, lockedUI, completedUI;

    public void SetupCell(DataManager.LevelData data, bool locked)
    {
        _levelData = data;
        _locked = locked;
        lvlNumber.text = data.name.ToString();
        Color aColor = data.color;
        aColor.a = 0.4f;
        border.color = aColor;
        perfectUI.color = aColor;
        completedUI.color = aColor;
        GetComponent<Image>().color = data.color;
        
        switch (data.state)
        {
            case DataManager.LevelData.LevelState.UNCOMPLETED:
                if (locked)
                    this.lockedUI.enabled = true;
                break;
            case DataManager.LevelData.LevelState.COMPLETED:
                completedUI.enabled = true;
                break;
            case DataManager.LevelData.LevelState.PERFECT:
                perfectUI.enabled = true;
                break;
        }
    }

    public void LoadLevel()
    {
        if (!_locked)
            GameManager.Instance.StartLevel(_levelData);
    }
}