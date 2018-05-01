import java.util.concurrent.ConcurrentHashMap;

public class SharedHashMap
{
	private static final ConcurrentHashMap instance = new ConcurrentHashMap();

	public static ConcurrentHashMap GetInstance()
	{
		return instance;
	}
}