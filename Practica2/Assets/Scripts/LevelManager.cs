using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine.Serialization;
using UnityEngine;
using UnityEngine.UI;


public class LevelManager : MonoBehaviour
{

    [SerializeField] private BoardManager boardManager;
    [SerializeField] private Text text;

    //TODO(Ricky): this will not be an update later on, i think maybe board can delegate
    public void Update()
    {
        text.text ="Steps " + boardManager.GetStepCount().ToString();
    }

    public void LoadLevel(DataManager.LevelCellData data)
    {
        boardManager.SetupLevel(data.data);
    }
}
