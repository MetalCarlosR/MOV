using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine.Serialization;
using UnityEngine;
using UnityEngine.UI;
using TMPro;
using UnityEditor;

public class LevelManager : MonoBehaviour
{
    [SerializeField] private BoardManager boardManager;

    [SerializeField] private TextMeshProUGUI levelTag;
    [SerializeField] private TextMeshProUGUI levelSize;
    
    [SerializeField] private TextMeshProUGUI connectedFlows;
    [SerializeField] private TextMeshProUGUI stepCount;
    [SerializeField] private TextMeshProUGUI progress;
    
    [SerializeField] private TextMeshProUGUI clues;
    [SerializeField] private Button cluesButton;
    [SerializeField] private Button undoButton;
    
    [SerializeField] private GameObject finishPanel;

    // TODO(Nico): hace falta enviarlo y que lo muestre todo guapete
    private int best = -1;
    private int totalFlows = 0;

    public void LoadLevel(DataManager.LevelData data)
    {
        var lvl = PuzzleParser.ParsePuzzle(data.data);
        boardManager.SetupLevel(lvl);
        totalFlows = lvl.FlowCount;
        SetLevelName(lvl.Width + "x" + lvl.Height);
        SetLevelColor(data.color);
        SetLevelNumber(data.name);

        SetConnectedFlowsText(0);
        SetStepsText(0);
        SetProgressText(0);
    }

    private void SetLevelColor(Color dataColor)
    {
        levelTag.color = dataColor;
    }

    private void SetLevelNumber(int number)
    {
        levelTag.text = "Nivel " + number.ToString();
    }

    public void SetConnectedFlowsText(int actualFlows)
    {
        connectedFlows.text = "Flows: " + actualFlows + "/" + totalFlows;
    }

    public void SetStepsText(int steps)
    {
        string bestStr = (best > 0 ? best.ToString() : "-");
        stepCount.text = "Moves: " + steps + " Best: " + bestStr;
    }

    public void SetProgressText(int progress)
    {
        this.progress.text = "Pipe: " + progress + "%";
    }

    public void SetCluesText(int clues)
    {
        this.clues.text = clues + "X";
    }

    private void SetLevelName(string name)
    {
        levelSize.text = name;
    }
    public void GoBackCallback()
    {
        GameManager.Instance.LoadScene(1);
    }

    public void GoNextCallback()
    {
        GameManager.Instance.NextLevel();
    }

    public void UseClue()
    {
        if (GameManager.Instance.ConsumeClue())
        {
            if (!boardManager.UseClue())
                cluesButton.interactable = false;
        }
    }

    public void EnableUndo()
    {
        undoButton.interactable = true;
    }

    public void GameFinished()
    {
        finishPanel.SetActive(true);
    }
}