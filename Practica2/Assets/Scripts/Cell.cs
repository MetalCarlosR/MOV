using UnityEditor.Il2Cpp;
using UnityEngine;

/**
 * This class will handle the visual representation of the flow in each cell
 */
public class Cell : MonoBehaviour
{

    [SerializeField]
    private GameObject circle;
    private SpriteRenderer _circleSr;

    [SerializeField]
    private SpriteRenderer up;

    [SerializeField]
    private SpriteRenderer down;
    [SerializeField]
    private SpriteRenderer left;
    [SerializeField]
    private SpriteRenderer right;
    
    [SerializeField]
    private SpriteRenderer background;

    private Color _color;
    private bool _isCircle = false;
    private bool _inUse = false;
    private int _x;
    private int _y;

    private void Awake()
    {
     _circleSr = circle.GetComponent<SpriteRenderer>();
    }

    /**
     * Sets the sprite to render a circle of the given color
     */
    public void SetCircle(Color c)
    {
        circle.SetActive(true);
        _circleSr.color = c;
         _color = c;
        right.color = c;
        left.color = c;
        up.color = c;
        down.color = c;
        _isCircle = true;
    }
    public bool IsCircle()
    {
     return _isCircle;
    }
    public bool IsInUse()
    {
        return _inUse;
        
    }
    public Color GetColor()
    {
     return _color;
    }

    public void SetColor(Color c)
    {
        _circleSr.color = c;
        _color = c;
        right.color = c;
        left.color = c;
        up.color = c;
        down.color = c;
    }

    public void ConnectUp()
    {
        up.enabled = true;
        _inUse = true;
    }

    public void DisconnectUp()
    {
        up.enabled = false;
    }

    public void ConnectDown()
    {
        down.enabled = true;
        _inUse = true;
    }

    public void DisconnectDown()
    {
        down.enabled = false;
    }

    public void ConnectLeft()
    {
        left.enabled = true;
        _inUse = true;
    }

    public void DisconnectLeft()
    {
        left.enabled = false;
    }

    public void ConnectRight()
    {
        right.enabled = true;
        _inUse = true;
    }

    public void DisconnectRight()
    {
        right.enabled = false;
    }

    public void Fill()
    {
        background.enabled = true;
        background.color = _color;
    }
    public void ResetCell()
    {
        up.enabled = false;
        down.enabled = false;
        left.enabled = false;
        right.enabled = false;
        _inUse = false;
        background.enabled = false;
        if(!_isCircle)
            _color = Color.black;
        DespawnMiniCircle();
    }

    public void SpawnMiniCircle()
    {
        if (!_isCircle)
        {
            circle.transform.localScale = new Vector3(0.9f, 0.9f, 1.0f);
            circle.SetActive(true);
            _circleSr.color = _color;
        }
    }

    public void DespawnMiniCircle()
    {
        if(!_isCircle)
            circle.SetActive(false);
    }

    public void SetCoords(int i, int j)
    {
        _x = i;
        _y = j;
    }

    public void GetCoords(out int i, out int j)
    {
        i = _x;
        j = _y;
    }
}