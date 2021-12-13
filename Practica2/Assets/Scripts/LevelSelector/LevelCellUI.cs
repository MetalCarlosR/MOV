using System.Collections;
using System.Collections.Generic;
using TMPro;
using UnityEngine;
using UnityEngine.UI;

public class LevelCellUI : MonoBehaviour
{
    private string _level;
    [SerializeField] private TextMeshProUGUI lvlNumber;
    [SerializeField] private Image border;

    public void SetupCell(string level, int number, Color color)
    {
        _level = level;
        lvlNumber.text = number.ToString();
        border.color = color;
        GetComponent<Image>().color = color;
    }

    public void LoadLevel()
    {
        
    }
}