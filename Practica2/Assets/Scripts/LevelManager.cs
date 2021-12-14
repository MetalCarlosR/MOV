using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine.Serialization;
using UnityEngine;
using UnityEngine.UI;


public class LevelManager : MonoBehaviour
{
    public struct LevelData
    {
        public string data;
        public string name;
        public Color color;

        public LevelData(string data, string name, Color color)
        {
            this.data = data;
            this.name = name;
            this.color = color;
        }
    }
    
    [SerializeField] private BoardManager boardManager;
    [SerializeField] private Text text;

    //TODO(Ricky): this will not be an update later on, i think maybe board can delegate
    public void Update()
    {
        text.text ="Steps " + boardManager.GetStepCount().ToString();
    }

    public void LoadLevel(LevelData data)
    {
        boardManager.SetupLevel(data.data);
    }
}
