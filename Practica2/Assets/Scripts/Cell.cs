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
    private GameObject right;
    [SerializeField]
    private GameObject left;
    [SerializeField]
    private GameObject up;
    [SerializeField]
    private GameObject down;

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

    //TODO: Estos métodos harán que se vea bien el flujo. Ale, que los implemente otro
    public void ConnectUp()
    {
     up.SetActive(true);
    }

    public void ConnectDown()
    {
     down.SetActive(true);
    }

    public void ConnectLeft()
    {
      left.SetActive(true);
    }

    public void ConnectRight()
    {
     right.SetActive(true);
    }

    public void Clear()
    {
     right.SetActive(false);
     left.SetActive(false);
     up.SetActive(false);
     down.SetActive(false);
    }
}