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

    private Color _color;
    private bool _isCircle = false;
    
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

    //TODO: Estos métodos harán que se vea bien el flujo. Ale, que los implemente otro
    public void ConnectUp()
    {
        up.enabled = true;
    }

    public void ConnectDown()
    {
        down.enabled = true;
    }

    public void ConnectLeft()
    {
        left.enabled = true;
    }

    public void ConnectRight()
    {
        right.enabled = true;
    }

    public void Clear()
    {
        up.enabled = true;
        down.enabled = true;
        left.enabled = true;
        right.enabled = true;
    }
}