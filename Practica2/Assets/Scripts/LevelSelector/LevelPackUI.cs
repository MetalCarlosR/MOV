using TMPro;
using UnityEngine;

public class LevelPackUI : MonoBehaviour
{
    [SerializeField] private TextMeshProUGUI levelName, levelCounter;
    private LevelPack _pack;

    public void SetupPack(LevelPack pack, Color color)
    {
        levelName.text = pack.name;
        levelName.color = color;
        levelCounter.text = 0 + "/" + (pack.pages.Length * 30);
        _pack = pack;
    }

    public void ShowPack()
    {
        GameManager.Instance.levelSelectorManager.LoadPackGrid(_pack);
    }
}
