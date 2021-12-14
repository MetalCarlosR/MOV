using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;
using UnityEngine.Serialization;

public class GameManager : MonoBehaviour
{
    public static GameManager Instance;
    public LevelSelectorManager levelSelectorManager = null;
    public LevelManager levelManager = null;
    private LevelManager.LevelData _dataToLoad;

    private void Awake()
    {
        if (Instance)
        {
            //TODO pasarle las cosas
            Instance.levelSelectorManager = levelSelectorManager;
            Instance.levelManager = levelManager;
            if(levelManager)
                levelManager.LoadLevel(Instance._dataToLoad);
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

    public void StartLevel(LevelManager.LevelData data)
    {
        _dataToLoad = data;
        LoadScene(2);
    }
    
}
