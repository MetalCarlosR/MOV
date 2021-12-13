using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.Serialization;

public class GameManager : MonoBehaviour
{
    public static GameManager Instance;
    public LevelSelectorManager levelSelectorManager;

    private void Awake()
    {
        if (Instance)
        {
            //TODO pasarle las cosas
            Instance.levelSelectorManager = levelSelectorManager;
            Destroy(gameObject);
        }
        else
        {
            Instance = this;
            DontDestroyOnLoad(this);
        }
    }
}
