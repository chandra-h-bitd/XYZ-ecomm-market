package com.aniruddha.XYZecommmarket.dao;

import com.aniruddha.XYZecommmarket.beans.Product;
import com.aniruddha.XYZecommmarket.cache.ProductCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class ProductRepository {
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    ProductCache productCache;

    private static final String baseSQL = "Select p.id as PRODUCT_ID,p.name,size,color,b.name as BRAND,pt.name as TYPE,s.name as SELLER,available_count,price " +
            "from Product p,Brand b,Supplier s,Producttype pt \n" +
            "where p.brand=b.id and p.type=pt.id and p.supplier=s.id ";

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Cacheable(value="products")
    public List<Product> getAllProducts() {
        return jdbcTemplate.query(baseSQL, (resultSet, i) -> {
            return mapToProduct(resultSet);
        });
    }

    @Cacheable(value="productsByBrands", key="#brandName")
    public List<Product> getProductsByBrand(String brandName) {
        String sql = baseSQL + "and upper(b.name)=upper(?)";
        return jdbcTemplate.query(sql, new Object[]{brandName}, (resultSet, i) -> {
            return mapToProduct(resultSet);
        });
    }

    @Cacheable(value="products", key="#maxPrice")
    public List<Product> getProductsByPriceRange(Double minPrice, Double maxPrice) {
        String sql = baseSQL + "and price > ? and price < ?";
        return jdbcTemplate.query(sql, new Object[]{minPrice, maxPrice}, (resultSet, i) -> {
            return mapToProduct(resultSet);
        });
    }

    @Cacheable(value="products", key="#productId")
    public Product getProductBySKU(Integer productId) {
        String sql = baseSQL + "and p.id=?";
        return jdbcTemplate.queryForObject(sql, new Object[]{productId},
                (resultSet, i) -> {
                    return mapToProduct(resultSet);
                });
    }

    @Cacheable(value="productCountBySeller", key="#sellerId")
    public Integer getProductCountByIdAndSeller(Integer productId, Integer sellerId) {
        String sql = baseSQL + "and p.id=? and s.id=?";
        return jdbcTemplate.query(sql, new Object[]{productId, sellerId},
                (resultSet, i) -> {
                    return mapToProduct(resultSet);
                }).size();
    }

    @Cacheable(value="products", key="#size")
    public List<Product> getProductsByTypeAndSize(Integer productType, Integer size) {
        String sql = baseSQL + "and pt.id=? and size=?";
        return jdbcTemplate.query(sql, new Object[]{productType, size},
                (resultSet, i) -> {
                    return mapToProduct(resultSet);
                });
    }

    @Cacheable(value="products", key="#color")
    public List<Product> getProductsByTypeAndColor(Integer productType, String color) {
        String sql = baseSQL + "and pt.id=? and upper(color)=upper(?)";
        return jdbcTemplate.query(sql, new Object[]{productType, color},
                (resultSet, i) -> {
                    return mapToProduct(resultSet);
                });
    }

    /**
     Add a  new brand through a new product entry
     */
    public void addBrand(Product product) {
        //refresh products by brand cache
        productCache.evictSingleCacheValue("productsByBrands","brandName");
        //TODO add brand code
    }

    /**
     Add a  new Supplier through a new product entry
     */
    public void addSupplier(Product product) {
        //refresh products by seller cache
        productCache.evictSingleCacheValue("productCountBySeller","sellerId");
        //TODO add supplier code
    }


    /**
     Add a  new Supplier through a new product entry
     */
    public void addProduct(Product product) {

        //refresh all products cache
        productCache.evictAllCacheValues("products");
        //TODO add product code
    }

    private Product mapToProduct(ResultSet resultSet) throws SQLException {
        Product product = new Product();
        product.setId(resultSet.getInt("PRODUCT_ID"));
        product.setName(resultSet.getString("NAME"));
        product.setSize(resultSet.getInt("SIZE"));
        product.setColor(resultSet.getString("COLOR"));
        product.setBrand(resultSet.getString("BRAND"));
        product.setType(resultSet.getString("TYPE"));
        product.setSupplier(resultSet.getString("SELLER"));
        product.setAvailableCount(resultSet.getInt("AVAILABLE_COUNT"));
        product.setPrice(resultSet.getDouble("PRICE"));

        return product;
    }
}
