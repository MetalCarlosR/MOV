using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.Advertisements;
using UnityEngine.SceneManagement;
using Random = UnityEngine.Random;

public class GameManager : MonoBehaviour
{
    public static GameManager Instance { get; private set; }
    public LevelSelectorManager levelSelectorManager = null;
    public LevelManager levelManager = null;
    private DataManager.LevelData _currentLevel;

    [SerializeField] private Skin currentSkin;

    [SerializeField] private AdManager adManager;

    private void Awake()
    {
        if (Instance)
        {
            Instance.levelSelectorManager = levelSelectorManager;
            Instance.levelManager = levelManager;
            
            if (levelManager)
            {
                levelManager.LoadLevel(Instance._currentLevel);
            }
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
    
    public void addClue()
    {
        updateClue(1);
    }

    public bool ConsumeClue()
    {
        if(DataManager.clues > 0)
        {
            updateClue(-1);
            return true;
        }
        return false;
    }

    private void updateClue(int inc)
    {
        DataManager.clues += inc;
        if (levelManager != null)
            levelManager.SetCluesText(DataManager.clues);
    }

    private void OnDestroy()
    {
        if(Instance == this)
            DataManager.SaveCurrentData();
    }

    public void NextLevel(bool win, bool previous)
    {
        if (DataManager.ThereIsNextLevel(_currentLevel, previous))
        {
            if(win)
                adManager.ShowInterstitialAd();
            StartLevel(DataManager.NextLevel(_currentLevel, previous));
        }
    }

    public void RefreshLevel()
    {
        LoadScene(2);
        levelManager.LoadLevel(Instance._currentLevel);
    }

    public void LevelFinished(bool perfect, int steps)
    {
        if(_currentLevel.state != DataManager.LevelData.LevelState.PERFECT)
            _currentLevel.state = perfect ? DataManager.LevelData.LevelState.PERFECT : DataManager.LevelData.LevelState.COMPLETED;
        
        _currentLevel.bestMovements = steps;
        DataManager.LevelPassed(_currentLevel);
    }

    public Skin GetSkin()
    {
        return currentSkin;
    }

    public void SetSkin(Skin s)
    {
        currentSkin = s;
    }
    
    private void OnApplicationQuit()
    {
        if(Instance == this)
            DataManager.SaveCurrentData();
    }


    private void OnApplicationPause(bool pauseStatus)
    {
        if(pauseStatus && Instance == this)
            DataManager.SaveCurrentData();
    }
}
