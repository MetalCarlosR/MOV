using System.Collections;
using System.Collections.Generic;
using UnityEngine;

[CreateAssetMenu(menuName = "Flow/LevelPack")]
public class LevelPack : ScriptableObject
{
    public string name;
    public TextAsset file;
}
