package course.concurrency.m2_async.minPrice;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;

public class PriceAggregator {

    private PriceRetriever priceRetriever = new PriceRetriever();

    private ExecutorService executor = Executors.newCachedThreadPool();

    public void setPriceRetriever(PriceRetriever priceRetriever) {
        this.priceRetriever = priceRetriever;
    }

    private Collection<Long> shopIds = Set.of(10l, 45l, 66l, 345l, 234l, 333l, 67l, 123l, 768l);

    public void setShops(Collection<Long> shopIds) {
        this.shopIds = shopIds;
    }

    public double getMinPrice(long itemId) {
        // place for your code
        var pricesCF = shopIds.stream()
                .map(shopId -> CompletableFuture
                        .supplyAsync(() -> priceRetriever.getPrice(itemId, shopId), executor)
                        .exceptionally(throwable -> Double.NaN)
                        .completeOnTimeout(Double.NaN, 2900, TimeUnit.MILLISECONDS))
                .toList();

        CompletableFuture.allOf(pricesCF.toArray(CompletableFuture[]::new)).join();

        return pricesCF.stream()
                .mapToDouble(CompletableFuture::join)
                .filter(price -> !Double.isNaN(price))
                .min()
                .orElse(Double.NaN);
//        из ответа ->
//        var completableFutureList =
//                shopIds.stream().map(shopId ->
//                                CompletableFuture.supplyAsync(() -> priceRetriever.getPrice(itemId, shopId), executor)
//                                        .completeOnTimeout(Double.POSITIVE_INFINITY, 2900, TimeUnit.MILLISECONDS)
//                                        .exceptionally(ex -> Double.POSITIVE_INFINITY))
//                        .toList();
//
//        CompletableFuture
//                .allOf(completableFutureList.toArray(CompletableFuture[]::new))
//                .join();
//
//        return completableFutureList
//                .stream()
//                .mapToDouble(CompletableFuture::join)
//                .filter(Double::isFinite)
//                .min()
//                .orElse(Double.NaN);

    }
}
