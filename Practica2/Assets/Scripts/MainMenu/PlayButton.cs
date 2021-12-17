using System;
using UnityEngine;
using UnityEngine.UI;
using Random = UnityEngine.Random;

public class PlayButton : MonoBehaviour
{
    private void Awake()
    {
        Button b = GetComponent<Button>();
        if(!b)
            Debug.LogWarning("No button found in " + this);
        var block = new ColorBlock();
        block.normalColor = Color.white;
        block.highlightedColor = Color.white;
        block.selectedColor = Color.white;
        block.colorMultiplier = 1;
        block.fadeDuration = 0.1f;

        Color[] colors = GameManager.Instance.GetSkin().colors;
        block.pressedColor = colors[Random.Range(0, colors.Length)];
        b.colors = block;
    }

    public void Play()
    {
        GameManager.Instance.LoadScene(1);
    }
}
