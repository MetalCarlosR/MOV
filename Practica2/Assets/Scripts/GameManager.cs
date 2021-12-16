using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.Advertisements;
using UnityEngine.SceneManagement;

public class GameManager : MonoBehaviour
{
    public static GameManager Instance { get; private set; }
    public LevelSelectorManager levelSelectorManager = null;
    public LevelManager levelManager = null;
    private DataManager.LevelData _currentLevel;

    private void Awake()
    {
        if (Instance)
        {
            //TODO pasarle las cosas
            Instance.levelSelectorManager = levelSelectorManager;
            Instance.levelManager = levelManager;
            if(levelManager)
                levelManager.LoadLevel(Instance._currentLevel);
            Destroy(gameObject);
        }
        else
        {
            Instance = this;
            DontDestroyOnLoad(this);
        }
    }

    public void LoadScene(string sceneName)
    {
        LoadScene(SceneManager.GetSceneByName(sceneName).buildIndex);
    }

    public void LoadScene(int index)
    {
        SceneManager.LoadScene(index);
    }

    public void StartLevel(DataManager.LevelData data)
    {
        _currentLevel = data;
        LoadScene(2);
    }
}
