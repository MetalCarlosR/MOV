using System.Collections;
using System.Collections.Generic;
using UnityEngine;

[CreateAssetMenu(menuName = "Flow/Skin")]
public class Skin : ScriptableObject
{
    public string name;
    public Color[] colors = new Color[16];
}