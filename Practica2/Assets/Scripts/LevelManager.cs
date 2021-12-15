using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine.Serialization;
using UnityEngine;
using UnityEngine.UI;


public class LevelManager : MonoBehaviour
{
    [SerializeField] private BoardManager boardManager;
    [SerializeField] private Text StepCount;
    [SerializeField] private Text LevelName;
    [SerializeField] private Text LevelTag;

    //TODO(Ricky): this will not be an update later on, i think maybe board can delegate
    public void Update()
    {
        StepCount.text = "Steps " + boardManager.GetStepCount().ToString();
    }

    public void LoadLevel(DataManager.LevelCellData data)
    {
        var lvl = PuzzleParser.ParsePuzzle(data.data);
        boardManager.SetupLevel(lvl);
        SetLevelName(lvl.Width +"x"+lvl.Height);
        SetLevelColor(data.color);
        SetLevelNumber(data.index);
    }

    private void SetLevelColor(Color dataColor)
    {
        LevelTag.color = dataColor;
    }

    private void SetLevelNumber(int number)
    {
        LevelTag.text = "Nivel "+number.ToString();
    }

    
    private void SetLevelName(string name)
    {
        LevelName.text = name;
    }

    public void GoBackCallback()
    {
        GameManager.Instance.LoadScene(1);
    }
}