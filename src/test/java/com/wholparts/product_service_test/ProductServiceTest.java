package com.wholparts.product_service_test;

import com.wholparts.product_service.dto.*;
import com.wholparts.product_service.model.Product;
import com.wholparts.product_service.model.Unit;
import com.wholparts.product_service.repository.ProductRepository;
import com.wholparts.product_service.service.NcmCestService;
import com.wholparts.product_service.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private NcmCestService ncmCestService;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductCreationDTO creationDTO;
    private ProductUpdateDTO updateDTO;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        creationDTO = new ProductCreationDTO();
        creationDTO.setName("Produto Teste");
        creationDTO.setDescription("Descrição teste");
        creationDTO.setNcm("12345678");
        creationDTO.setNcmDescription("Descrição NCM");
        creationDTO.setUnit(Unit.UN);

        updateDTO = new ProductUpdateDTO();
        updateDTO.setName("Produto Atualizado");
        updateDTO.setDescription("Nova descrição");
        updateDTO.setNcm("87654321");
        updateDTO.setGtin("1234567890123");

        product = Product.builder()
                .id(1L)
                .name("Produto Teste")
                .description("Descrição teste")
                .ncm("12345678")
                .ncmDescription("Descrição NCM")
                .cest("C123")
                .unit(Unit.UN)
                .active(false)
                .autoCreated(false)
                .build();
    }

    @Test
    void create_deveCriarProduto() {
        when(ncmCestService.getCestByNcm("12345678")).thenReturn("C123");
        // Retorna exatamente o objeto que o service criou
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductViewDTO dto = productService.create(creationDTO);

        assertNotNull(dto);
        assertEquals("Produto Teste", dto.getName());
        assertEquals("C123", dto.getCest());
        assertTrue(dto.getActive()); // Agora passa corretamente
    }

    @Test
    void toViewDTO_deveMapearProduto() {
        ProductViewDTO dto = productService.toViewDTO(product);

        assertEquals(product.getId(), dto.getId());
        assertEquals(product.getName(), dto.getName());
        assertEquals(product.getDescription(), dto.getDescription());
        assertEquals(product.getUnit().name(), dto.getUnit());
        assertEquals(product.getNcm(), dto.getNcm());
        assertEquals(product.getCest(), dto.getCest());
        assertEquals(product.getNcmDescription(), dto.getNcmDescription());
    }

    @Test
    void getOrCreate_deveRetornarProdutoExistente() {
        when(productRepository.findByNameAndNcm("Produto Teste", "12345678"))
                .thenReturn(Optional.of(product));

        Product result = productService.getOrCreate(creationDTO);

        assertEquals(product, result);
    }

    @Test
    void getOrCreate_deveCriarNovoProdutoSeNaoExistir() {
        // Mock: não existe produto com o mesmo nome e NCM
        when(productRepository.findByNameAndNcm("Produto Teste", "12345678"))
                .thenReturn(Optional.empty());

        // Mock: retorna o mesmo produto que foi passado para save
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Executa o método
        Product result = productService.getOrCreate(creationDTO);

        // Verificações
        assertNotNull(result);
        assertTrue(result.getAutoCreated(), "Produto não foi marcado como auto-created");
        assertFalse(result.getActive(), "Produto não deve estar ativo por padrão");
    }










    @Test
    void findById_deveRetornarProduto() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductViewDTO dto = productService.findById(1L);

        assertNotNull(dto);
        assertEquals("Produto Teste", dto.getName());
    }

    @Test
    void findById_deveLancarExcecaoSeNaoEncontrar() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productService.findById(1L));
    }

    @Test
    void activate_deveAtivarProduto() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.activate(1L);

        assertTrue(product.getActive());
    }

    @Test
    void desactivate_deveDesativarProduto() {
        product.setActive(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.desactivate(1L);

        assertFalse(product.getActive());
    }

    @Test
    void update_deveAtualizarProduto() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(ncmCestService.getCestByNcm("87654321")).thenReturn("C999");

        ProductViewDTO dto = productService.update(1L, updateDTO);

        assertEquals("Produto Atualizado", dto.getName());
        assertEquals("Nova descrição", dto.getDescription());
        assertEquals("C999", dto.getCest());
        assertEquals("87654321", dto.getNcm());
        assertEquals("1234567890123", dto.getGtin());
    }

    @Test
    void findAllActive_deveRetornarProdutosAtivos() {
        product.setActive(true);
        when(productRepository.findAllByActiveTrue()).thenReturn(List.of(product));

        List<ProductViewDTO> list = productService.findAllActive();

        assertEquals(1, list.size());
        assertEquals(product.getName(), list.get(0).getName());
    }

    @Test
    void findAll_deveRetornarTodosProdutos() {
        when(productRepository.findAll()).thenReturn(List.of(product));

        List<ProductViewDTO> list = productService.findAll();

        assertEquals(1, list.size());
        assertEquals(product.getName(), list.get(0).getName());
    }

    @Test
    void findAutoCreated_deveRetornarProdutosAutoCriados() {
        product.setAutoCreated(true);
        when(productRepository.findAllByAutoCreatedTrue()).thenReturn(List.of(product));

        List<ProductViewDTO> list = productService.findAutoCreated();

        assertEquals(1, list.size());
        assertTrue(list.get(0).getName().contains("Produto Teste"));
    }
}
