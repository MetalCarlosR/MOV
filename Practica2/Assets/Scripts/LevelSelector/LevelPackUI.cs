using TMPro;
using UnityEngine;

public class LevelPackUI : MonoBehaviour
{
    [SerializeField] private TextMeshProUGUI levelName, levelCounter;
    private LevelPack _pack;
    private Color _color;
    private int _progress;
    
    /// <summary>
    /// Shows the state of the pack associated and stores the data for later call.
    /// </summary>
    /// <param name="pack">Data from the pack</param>
    /// <param name="color">Color of the pack</param>
    public void SetupPack(LevelPack pack, Color color)
    {
        DataManager.PackData data = DataManager.GetPackData(pack.name);
        levelName.text = pack.name;
        levelName.color = color;
        _pack = pack;
        _color = color;
        levelCounter.text = data.completed + "/" + (pack.pages.Length * 30);

    }
    
    /// <summary>
    /// Calls the setup of the pack in screen.
    /// </summary>
    public void ShowPack()
    {
        GameManager.Instance.levelSelectorManager.LoadPackGrid(_pack, _color);
    }
}
