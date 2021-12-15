using TMPro;
using UnityEngine;

public class LevelPackUI : MonoBehaviour
{
    [SerializeField] private TextMeshProUGUI levelName, levelCounter;
    private LevelPack _pack;
    private Color _color;
    private int _progress;

    public void SetupPack(LevelPack pack, Color color)
    {
        DataManager.PackData data = DataManager.GetPackData(pack.name);
        levelName.text = pack.name;
        levelName.color = color;
        _pack = pack;
        _color = color;
        levelCounter.text = data.completed + "/" + (pack.pages.Length * 30);

    }

    public void ShowPack()
    {
        GameManager.Instance.levelSelectorManager.LoadPackGrid(_pack, _color);
    }
}
