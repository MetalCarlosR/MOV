using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine.Serialization;
using UnityEngine;
using UnityEngine.UI;
using TMPro;

public class LevelManager : MonoBehaviour
{
    [SerializeField] private BoardManager boardManager;

    [SerializeField] private TextMeshProUGUI LevelTag;
    [SerializeField] private TextMeshProUGUI LevelSize;
    
    [SerializeField] private TextMeshProUGUI ConnectedFlows;
    [SerializeField] private TextMeshProUGUI StepCount;
    [SerializeField] private TextMeshProUGUI Progress;

    [SerializeField] private TextMeshProUGUI Clues;

    [SerializeField] private Button RewardedButton;

    // TODO(Nico): hace falta enviarlo y que lo muestre todo guapete
    private int best = -1;
    private int totalFlows = 0;

    public void LoadLevel(DataManager.LevelData data)
    {
        var lvl = PuzzleParser.ParsePuzzle(data.data);
        boardManager.SetupLevel(lvl);
        totalFlows = lvl.FlowCount;
        SetLevelName(lvl.Width +"x"+lvl.Height);
        SetLevelColor(data.color);
        SetLevelNumber(data.name);

        SetConnectedFlowsText(0);
        SetStepsText(0);
        SetProgressText(0);
    }

    private void SetLevelColor(Color dataColor)
    {
        LevelTag.color = dataColor;
    }

    private void SetLevelNumber(int number)
    {
        LevelTag.text = "Nivel "+number.ToString();
    }

    public void SetConnectedFlowsText(int actualFlows)
    {
        ConnectedFlows.text = "Flows: " + actualFlows + "/" + totalFlows;
    }

    public void SetStepsText(int steps)
    {
        string bestStr = (best > 0 ? best.ToString() : "-");
        StepCount.text = "Moves: " + steps + " Best: " + bestStr;
    }

    public void SetProgressText(int progress)
    {
        Progress.text = "Pipe: " + progress + "%";
    }

    public void SetCluesText(int clues)
    {
        Clues.text = clues + "X";
    }

    private void SetLevelName(string name)
    {
        LevelSize.text = name;
    }

    public void LinkRewardedAdWithButton(RewardedAdsButton rewardedAdButton)
    {
        if (RewardedButton == null)
            Debug.Log("ME TIRO POR UNA VENTANA");
        else Debug.Log("NO ME TIRO");
        rewardedAdButton.SetButton(RewardedButton);
    }

    public void GoBackCallback()
    {
        GameManager.Instance.LoadScene(1);
    }
}