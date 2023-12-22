package shopping;

import customer.Customer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import product.Product;
import product.ProductDao;

public class ShoppingServiceTest {

    private final ProductDao productDaoMock = Mockito.mock(ProductDao.class);
    private final ShoppingService shoppingService = new ShoppingServiceImpl(productDaoMock);

    /**
     * Тестирование совершения покупки.
     * Было проверено три случая:
     * 1. Если корзина пустая
     * 2. Если корзина полная
     * 3. Если товара больше нет или его не хватает
     * 4. Если добавляется отрицательное количество товаров
     */
    @Test
    public void buyMethodTest() throws BuyException {
        Customer firstCustomer = new Customer(1L, "7-982-555-64-64");
        Cart firstCustomersCart = new Cart(firstCustomer);

        // первоначально наша корзина пуста, поэтому нам следует это проверить
        Assertions.assertFalse(shoppingService.buy(firstCustomersCart));

        // теперь добавим какой-нибудь товар в корзину
        Product snickers = new Product("Сникерс Лесной Орех", 10);
        firstCustomersCart.add(snickers, 7);

        Assertions.assertTrue(shoppingService.buy(firstCustomersCart));

        // проверим, что был вызван метод save и количество продуктов уменьшилось
        Mockito.verify(productDaoMock).save(Mockito.argThat(
                (Product p) -> p.getName().equals("Сникерс Лесной Орех") && p.getCount() == 3));

        // изменим количество товара в корзине у первого покупателя
        firstCustomersCart.edit(snickers, 3);
        shoppingService.buy(firstCustomersCart);

        // товара остаться не должно, также должно выброситься исключение
        Exception buyException = Assertions.assertThrows(
                BuyException.class, () -> shoppingService.buy(firstCustomersCart)
        );

        Assertions.assertEquals(
                "В наличии нет необходимого количества товара 'Сникерс Лесной Орех'",
                buyException.getMessage());

        Customer secondCustomer = new Customer(1L, "7-982-777-64-64");

        Product mars = new Product("Марс", 3);

        Cart secondCart = shoppingService.getCart(secondCustomer);
        secondCart.add(mars, -3);

        // Данная проверка не пройдет, так как нет нужной проверки на
        // добавление отрицательного числа товаров в методе validateCount класса Cart
        Assertions.assertFalse(shoppingService.buy(secondCart));
        // ну и соответственно проверка, что метод save не вызовется
        Mockito.verify(productDaoMock, Mockito.never()).save(Mockito.any(Product.class));
    }

    /**
     * Метод получения корзины в тестировании не нуждается,
     * поскольку он переносит свою ответственность на конструктор {@link Cart}
     */
    @Test
    public void getCartMethodTest() {}

    /**
     * Метод получения полного перечня продуктов в тестировании не нуждается,
     * поскольку метод переносит свою ответственность на {@link ProductDao}
     */
    @Test
    public void getAllProductsMethodTest() {}

    /**
     * Метод получения продукта по имени в тестировании не нуждается,
     * поскольку метод переносит свою ответственность на {@link ProductDao}
     */
    @Test
    void getProductByNameMethodTest() {}
}
