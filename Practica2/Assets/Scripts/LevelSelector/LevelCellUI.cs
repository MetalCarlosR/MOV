using System.Collections;
using System.Collections.Generic;
using TMPro;
using UnityEngine;
using UnityEngine.UI;

public class LevelCellUI : MonoBehaviour
{
    private string _level;
    private Color _color;
    private int _number;
    [SerializeField] private TextMeshProUGUI lvlNumber;
    [SerializeField] private Image border;

    public void SetupCell(string level, int number, Color color)
    {
        _level = level;
        _color = color;
        _number = number;
        lvlNumber.text = number.ToString();
        border.color = color;
        GetComponent<Image>().color = color;
    }

    public void LoadLevel()
    {
        LevelManager.LevelData levelData = new LevelManager.LevelData(_level, _number.ToString(), _color);
        GameManager.Instance.StartLevel(levelData);
    }
}