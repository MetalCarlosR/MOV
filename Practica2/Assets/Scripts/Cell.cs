using UnityEngine;

/**
 * This class will handle the visual representation of the flow in each cell
 */
public class Cell : MonoBehaviour
{
    [SerializeField]
    private GameObject enterFlow;
    
    [SerializeField]
    private GameObject exitFlow;

    [SerializeField]
    private GameObject circle;

    private SpriteRenderer _circleSr;
    private SpriteRenderer _enterFlowSr;
    private SpriteRenderer _exitFlowSr;

    private Color _color;
    private bool _isCircle = false;
    
    private void Awake()
    {
     _circleSr = circle.GetComponent<SpriteRenderer>();
     _enterFlowSr = enterFlow.GetComponent<SpriteRenderer>();
     _exitFlowSr = exitFlow.GetComponent<SpriteRenderer>();
    }

    /**
     * Sets the sprite to render a circle of the given color
     */
    public void SetCircle(Color c)
    {
     enterFlow.SetActive(false);
     exitFlow.SetActive(false);
     circle.SetActive(true);

     _circleSr.color = c;
     _color = c;
     _isCircle = true;
    }
    /**
     * Renders enter flow with the given color and up vector direction
     */
    public void SetEnterFlow(Color c, Vector2 up)
    {
     enterFlow.SetActive(true);
     exitFlow.SetActive(false);
     circle.SetActive(false);

     
     _enterFlowSr.color = c;
     enterFlow.transform.up = up;
     _color = c;
    }
   
    /**
     * Renders exit flow with the given color and up vector direction
     */
    public void SetExitFlow(Color c, Vector2 up)
    {
     enterFlow.SetActive(false);
     exitFlow.SetActive(true);
     circle.SetActive(false);

     _exitFlowSr.color = c;
     exitFlow.transform.up = up;
     _color = c;
    }

    public Color GetColor()
    {
     return _color;
    }

    public bool IsCircle()
    {
     return _isCircle;
    }
}