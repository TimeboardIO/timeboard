package kronops.core.api;

import java.util.List;

/**
 * Base DAO
 * @param <K> primary key type
 * @param <T> object type
 */
public interface BaseDAO<K, T> {

    public T save(T object);


    public T delete(T object);

    public default T deleteById(K objectPkey){
        return this.delete(this.findById(objectPkey));
    }

    public T findById(K objectPkey);

    public List<T> findAll();

}
