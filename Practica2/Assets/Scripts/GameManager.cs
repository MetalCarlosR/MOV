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

    int _clue = 3;

    private void Awake()
    {
        if (Instance)
        {
            //TODO pasarle las cosas
            Instance.levelSelectorManager = levelSelectorManager;
            Instance.levelManager = levelManager;
            
            if (levelManager)
            {
                levelManager.LoadLevel(Instance._currentLevel);
                // //TODO BORRAR ESTE DEBUG
                // Instance._currentLevel.state = (DataManager.LevelData.LevelState)Random.Range(1,3);
                // Instance._currentLevel.bestMovements = Random.Range(5,12);
                // DataManager.LevelPassed(Instance._currentLevel);
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
        if(_clue > 0)
        {
            updateClue(-1);
            return true;
        }
        return false;
    }

    private void updateClue(int inc)
    {
        _clue += inc;
        if (levelManager != null)
            levelManager.SetCluesText(_clue);
    }

    private void OnDestroy()
    {
        if(Instance == this)
            DataManager.SaveCurrentData();
    }

    public void NextLevel()
    {
        if (DataManager.ThereIsNextLevel(_currentLevel))
        {
            StartLevel(DataManager.NextLevel(_currentLevel));
        }
    }

    public void LevelFinished(bool perfect, int steps)
    {
        if(_currentLevel.state != DataManager.LevelData.LevelState.PERFECT)
            _currentLevel.state = perfect ? DataManager.LevelData.LevelState.PERFECT : DataManager.LevelData.LevelState.COMPLETED;
        
        if(steps < _currentLevel.bestMovements)
            _currentLevel.bestMovements = steps;
        DataManager.LevelPassed(_currentLevel);
    }
}
