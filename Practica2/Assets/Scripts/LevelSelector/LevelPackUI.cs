using TMPro;
using UnityEngine;

public class LevelPackUI : MonoBehaviour
{
    [SerializeField] private TextMeshProUGUI levelName, levelCounter;

    public void SetupPack(LevelPack pack, Color color)
    {
        levelName.text = pack.name;
        levelName.color = color;
    }
}
