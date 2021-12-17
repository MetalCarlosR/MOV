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
    [SerializeField] private Button previousLevelButton;
    [SerializeField] private Button nextLevelButton;
    
    [SerializeField] private GameObject finishPanel;
    [SerializeField] private TextMeshProUGUI finishPanelStepCount;
    [SerializeField] private TextMeshProUGUI finishPanelTitle;

    [SerializeField] private RawImage finishPanelBar;
    [SerializeField] private RawImage finishPanelMiniBar;

    private int best = -1;
    private int totalFlows = 0;

    private Color _themeColor;
    
    public void LoadLevel(DataManager.LevelData data)
    {
        best = data.bestMovements;
        var lvl = PuzzleParser.ParsePuzzle(data.data);
        boardManager.SetupLevel(lvl);
        totalFlows = lvl.FlowCount;
        SetLevelName(lvl.Width + "x" + lvl.Height);
        SetLevelColor(data.color);
        SetLevelNumber(data.name);

        _themeColor = data.color;

        SetCluesText(DataManager.clues);
        SetConnectedFlowsText(0);
        SetStepsText(0);
        SetProgressText(0);

        nextLevelButton.interactable = GameManager.Instance.ThereIsNextLevel();
        previousLevelButton.interactable = GameManager.Instance.ThereIsNextLevel(true);
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

    public void GoNextCallback(bool win)
    {
        GameManager.Instance.NextLevel(win, false);
    }

    public void GoPreviousCallback()
    {
        GameManager.Instance.NextLevel(false, true);
    }

    public void RefreshLevelCallback()
    {
        GameManager.Instance.RefreshLevel();
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

    public void GameFinished(bool perfect, int count)
    {
        finishPanel.SetActive(true);

        if (best > count)
            SetStepsText(count);

        finishPanelStepCount.text = $"Has completado el nivel en {count} pasos";
        finishPanelTitle.text = (perfect ? "¡Perfecto!" : "Nivel Completado");
        
        Color aux = _themeColor;
        aux.a = 0.4f;
        
        finishPanelBar.color = aux;
        finishPanelMiniBar.color = _themeColor;

        GameManager.Instance.LevelFinished(perfect, count);
    }
}