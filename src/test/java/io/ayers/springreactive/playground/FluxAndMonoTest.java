package io.ayers.springreactive.playground;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.test.scheduler.VirtualTimeScheduler;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class FluxAndMonoTest {

    List<String> names = Arrays.asList("adam", "jack", "jenny", "anna");

    @Test
    void FluxTest_WithoutError() {
        var stringFlux = Flux.just("Spring", "Spring Boot", "Reactive")
                             .log();

        StepVerifier.create(stringFlux)
                    .expectNextCount(3)
                    .verifyComplete();

        StepVerifier.create(stringFlux)
                    .expectNext("Spring")
                    .expectNext("Spring Boot")
                    .expectNext("Reactive")
                    .verifyComplete();
    }

    @Test
    void FluxTest_WithError() {
        var stringFlux = Flux.just("Spring", "Spring Boot", "Reactive")
                             .concatWith(Flux.error(new RuntimeException("Exception Occurred!")))
                             .log();

        StepVerifier.create(stringFlux)
                    .expectNext("Spring")
                    .expectNext("Spring Boot")
                    .expectNext("Reactive")
                    .expectError(RuntimeException.class)
//                    .expectErrorMessage("Exception Occurred!")
                    .verify();
    }

    @Test
    void MonoTest() {
        var stringMono = Mono.just("Spring")
                             .log();

        StepVerifier.create(stringMono)
                    .expectNextCount(1)
                    .verifyComplete();

        StepVerifier.create(stringMono)
                    .expectNext("Spring")
                    .verifyComplete();

    }

    @Test
    void FluxUsingIterable() {
        var stringFlux = Flux.fromIterable(names);
        StepVerifier.create(stringFlux)
                    .expectNext("adam", "jack", "jenny", "anna")
                    .verifyComplete();
    }

    @Test
    void FluxFromArray() {
        String[] names = new String[]{"jeff", "jam"};
        var stringFlux = Flux.fromArray(names);
        StepVerifier.create(stringFlux)
                    .expectNext(names)
                    .verifyComplete();
    }

    @Test
    void FluxUsingStream() {
        var stringFlux = Flux.fromStream(names.stream());
        StepVerifier.create(stringFlux)
                    .expectNext("adam", "jack", "jenny", "anna")
                    .verifyComplete();
    }

    @Test
    void MonoJustOrEmpty() {
        var objectMono = Mono.justOrEmpty(Optional.empty());
        StepVerifier.create(objectMono)
                    .verifyComplete();
    }

    @Test
    void MonoUsingSupplier() {
        Supplier<String> stringSupplier = () -> "adam";
        var stringMono = Mono.fromSupplier(stringSupplier);
        StepVerifier.create(stringMono)
                    .expectNext(stringSupplier.get())
                    .verifyComplete();
    }

    @Test
    void FluxUsingRange() {
        var range = Flux.range(1, 100)
                        .log();
        StepVerifier.create(range)
                    .expectNextCount(100)
                    .verifyComplete();
    }

    @Test
    void FluxFilter() {
        var stringFlux = Flux.fromIterable(names)
                             .filter(s -> s.startsWith("a"))
                             .log();
        StepVerifier.create(stringFlux)
                    .expectNext("adam")
                    .expectNext("anna")
                    .verifyComplete();

    }

    @Test
    void FluxTransformUsingMap() {
        var stringFlux = Flux.fromIterable(names)
                             .map(String::toUpperCase)
                             .log();
        StepVerifier.create(stringFlux)
                    .expectNext(names.stream()
                                     .map(String::toUpperCase)
                                     .toArray(String[]::new))
                    .verifyComplete();
    }

    @Test
    void FluxTransformUsingFlatMap() {
        var stringFlux = Flux.fromIterable(names)
                             .flatMap(s -> Flux.fromIterable(convertToList(s)))
                             .log();
        StepVerifier.create(stringFlux)
                    .expectNextCount(8)
                    .verifyComplete();
    }

    @Test
    void FluxTransformUsingFlatMap_usingParallel() {
        var stringFlux = Flux.fromIterable(names)
                             .window(2)
                             .flatMap(s -> s.map(this::convertToList)
                                            .subscribeOn(Schedulers.parallel()))
                             .flatMap(Flux::fromIterable)
                             .log();
        StepVerifier.create(stringFlux)
                    .expectNextCount(8)
                    .verifyComplete();
    }

    @Test
    void FluxTransformUsingFlatMap_usingParallel_maintainOrder() {
        var stringFlux = Flux.fromIterable(names)
                             .window(2)
                             .flatMapSequential(s -> s.map(this::convertToList)
                                                      .subscribeOn(Schedulers.parallel()))
                             .flatMap(Flux::fromIterable)
                             .log();
        StepVerifier.create(stringFlux)
                    .expectNextCount(8)
                    .verifyComplete();
    }

    @Test
    void FluxAndMonoCombine_UsingMerge() {
        var flux1 = Flux.just("A", "B");
        var flux2 = Flux.just("C", "D");
        var merge = Flux.merge(flux1, flux2);

        StepVerifier.create(merge)
                    .expectSubscription()
                    .expectNext("A", "B", "C", "D")
                    .verifyComplete();

    }

    @Test
    void FluxAndMonoCombine_UsingConcat() {
        var flux1 = Flux.just("A", "B");
        var flux2 = Flux.just("C", "D");
        var concat = Flux.concat(flux1, flux2);

        StepVerifier.create(concat)
                    .expectSubscription()
                    .expectNext("A", "B", "C", "D")
                    .verifyComplete();

    }

    @Test
    void FluxAndMonoCombine_UsingZip() {
        var flux1 = Flux.just("A", "B");
        var flux2 = Flux.just("C", "D");
        var zip = Flux.zip(flux1, flux2, String::concat);

        StepVerifier.create(zip)
                    .expectSubscription()
                    .expectNext("AC", "BD")
                    .verifyComplete();

    }

    @Test
    void FluxErrorHandling() {
        var integerFlux =
                Flux.just(1, 2, 3)
                    .concatWith(Flux.error(new RuntimeException("Error!!!")))
                    .concatWith(Flux.just(4))
                    .onErrorResume(throwable -> Flux.just(5));

        StepVerifier.create(integerFlux)
                    .expectSubscription()
                    .expectNext(1, 2, 3, 5)
                    .verifyComplete();

    }

    @Test
    void FluxErrorHandling_ErrorReturn() {
        var integerFlux =
                Flux.just(1, 2, 3)
                    .concatWith(Flux.error(new RuntimeException("Error!!!")))
                    .concatWith(Flux.just(4))
                    .onErrorReturn(5);

        StepVerifier.create(integerFlux)
                    .expectSubscription()
                    .expectNext(1, 2, 3, 5)
                    .verifyComplete();

    }

    @Test
    void FluxErrorHandling_ErrorMap() {
        var integerFlux =
                Flux.just(1, 2, 3)
                    .concatWith(Flux.error(new RuntimeException("Error!!!")))
                    .concatWith(Flux.just(4))
                    .onErrorMap(CustomException::new);

        StepVerifier.create(integerFlux)
                    .expectSubscription()
                    .expectNext(1, 2, 3)
                    .expectError(CustomException.class)
                    .verify();

    }

    @Test
    void FluxErrorHandling_ErrorMap_withRetry() {
        var integerFlux =
                Flux.just(1, 2, 3)
                    .concatWith(Flux.error(new RuntimeException("Error!!!")))
                    .concatWith(Flux.just(4))
                    .onErrorMap(CustomException::new)
                    .retry(2);

        StepVerifier.create(integerFlux)
                    .expectSubscription()
                    .expectNext(1, 2, 3)
                    .expectNext(1, 2, 3)
                    .expectNext(1, 2, 3)
                    .expectError(CustomException.class)
                    .verify();

    }

    @Test
    void FluxErrorHandling_ErrorMap_withRetryBackoff() {
        var integerFlux =
                Flux.just(1, 2, 3)
                    .concatWith(Flux.error(new RuntimeException("Error!!!")))
                    .concatWith(Flux.just(4))
                    .onErrorMap(CustomException::new)
                    .retryWhen(Retry.backoff(2, Duration.ofSeconds(2)));

        StepVerifier.create(integerFlux)
                    .expectSubscription()
                    .expectNext(1, 2, 3)
                    .expectNext(1, 2, 3)
                    .expectNext(1, 2, 3)
                    .expectError(IllegalStateException.class)
                    .verify();

    }

    @Test
    void Backpressure() {
        var integerFlux = Flux.range(1, 10)
                              .log();

        StepVerifier.create(integerFlux)
                    .expectSubscription()
                    .thenRequest(1)
                    .expectNext(1)
                    .thenRequest(1)
                    .expectNext(2)
                    .thenCancel()
                    .verify();
    }

    @Test
    void Backpressure_Programmatically() {
        var integerFlux = Flux.range(1, 10)
                              .log();

        // Deprecated
        integerFlux.subscribe(
                System.out::println,
                System.err::println,
                System.out::println,
                subscription -> subscription.request(2));
    }

    @Test
    void Backpressure_Cancel() {
        var integerFlux = Flux.range(1, 10)
                              .log();

        // Deprecated
        integerFlux.subscribe(
                System.out::println,
                System.err::println,
                System.out::println,
                Subscription::cancel);
    }

    @Test
    void Backpressure_Custom() {
        var integerFlux = Flux.range(1, 10)
                              .log();

        integerFlux.subscribe(new BaseSubscriber<>() {
            @Override
            protected void hookOnNext(Integer value) {
                request(1);
                System.out.println("Value received is : " + value);
                if (value == 4) {
                    cancel();
                }
            }
        });
    }

    @Test
    void HotPublisher() throws InterruptedException {
        Flux<Integer> integerFlux = Flux.just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                                        .delayElements(Duration.ofSeconds(1));

        ConnectableFlux<Integer> connectableFlux = integerFlux.publish();
        connectableFlux.connect();

        connectableFlux.subscribe(integer -> System.out.println("Subscriber 1:" + integer));

        Thread.sleep(3000);

        connectableFlux.subscribe(integer -> System.out.println("Subscriber 2:" + integer));

        Thread.sleep(10000);
    }

    @Test
    void Flux_WithoutVirtualTime() throws InterruptedException {
        Flux<Long> longFlux = Flux.interval(Duration.ofSeconds(1))
                                  .take(3)
                                  .log();

        StepVerifier.create(longFlux)
                    .expectSubscription()
                    .expectNext(0L, 1L, 2L)
                    .verifyComplete();
    }

    @Test
    void Flux_WithVirtualTime() throws InterruptedException {

        VirtualTimeScheduler.getOrSet();

        Flux<Long> longFlux = Flux.interval(Duration.ofSeconds(1))
                                  .take(3)
                                  .log();

        StepVerifier.withVirtualTime(longFlux::log)
                    .expectSubscription()
                    .thenAwait(Duration.ofSeconds(3))
                    .expectNext(0L, 1L, 2L)
                    .verifyComplete();
    }

    private List<String> convertToList(String s) {

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return List.of(s, "newVal");
    }

    private static class CustomException
            extends Throwable {

        private String message;

        public CustomException(Throwable e) {
            this.message = e.getMessage();
        }

        @Override
        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
