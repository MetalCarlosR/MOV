using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

[CreateAssetMenu(menuName = "Flow/LevelPack")]
public class LevelPack : ScriptableObject
{
    public new string name;
    public TextAsset file;
    public bool locked;
    
    [Serializable]
    public struct LevelPage
    {
        public string pageName;
        public Color pageColor;
    }

    public LevelPage[] pages;
}
