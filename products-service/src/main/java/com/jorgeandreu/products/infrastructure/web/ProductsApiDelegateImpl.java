package com.jorgeandreu.products.infrastructure.web;

import com.jorgeandreu.products.domain.model.PageResult;
import com.jorgeandreu.products.domain.model.Product;
import com.jorgeandreu.products.domain.port.in.CreateProductUseCase;
import com.jorgeandreu.products.domain.port.in.DeleteProductUseCase;
import com.jorgeandreu.products.domain.port.in.GetProductUseCase;
import com.jorgeandreu.products.domain.port.in.ListProductsUseCase;
import com.jorgeandreu.products.infrastructure.api.ProductsApiDelegate;
import com.jorgeandreu.products.infrastructure.api.model.CreateProductRequest;
import com.jorgeandreu.products.infrastructure.api.model.ProductPage;
import com.jorgeandreu.products.infrastructure.api.model.ProductSearchCriteriaRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductsApiDelegateImpl implements ProductsApiDelegate {


    private final GetProductUseCase getProduct;

    private final ProductWebMapper webMapper;

    private final CreateProductUseCase createProductUC;

    private final ListProductsUseCase listProductUC;

    private final DeleteProductUseCase deleteProductUC;

    @Override
    public ResponseEntity<com.jorgeandreu.products.infrastructure.api.model.Product> createProduct(CreateProductRequest req) {
        var cmd = webMapper.toCommand(req);

        Product created = createProductUC.create(cmd);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(webMapper.toApi(created));
    }

    @Override
    public ResponseEntity<com.jorgeandreu.products.infrastructure.api.model.Product> getProductById(UUID id) {
        Product product = getProduct.getById(id);
        return ResponseEntity.ok(webMapper.toApi(product));
    }

    @Override
    public ResponseEntity<ProductPage> searchProducts(ProductSearchCriteriaRequest productCriteria) {
        var criteria = webMapper.productSearchCriteriaToSearchCriteria(productCriteria);
        PageResult<Product> page = listProductUC.list(criteria);
        return ResponseEntity.ok().body(webMapper.toApi(page));
    }

    @Override
    public ResponseEntity<Void> deleteProductById(UUID id) {
        deleteProductUC.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
