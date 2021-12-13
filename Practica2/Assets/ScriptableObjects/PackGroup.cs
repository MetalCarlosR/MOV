using System.Collections;
using System.Collections.Generic;
using UnityEngine;

[CreateAssetMenu(menuName = "Flow/PackGroup")]
public class PackGroup : ScriptableObject
{
    public new string name;
    public LevelPack[] packs;
    public Color color;
}