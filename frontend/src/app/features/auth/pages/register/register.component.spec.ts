import { beforeEach, describe, expect, it } from "vitest";
import { RegisterComponent } from "./register.component";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { provideHttpClient } from "@angular/common/http";
import { provideRouter } from "@angular/router";

describe("RegisterComponent", () => {
    let component: RegisterComponent;
    let fixture: ComponentFixture<RegisterComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [RegisterComponent],
            providers: [provideHttpClient(), provideRouter([])]
        })
            .compileComponents();

        fixture = TestBed.createComponent(RegisterComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    describe("component setup", () => {
        it("should create", () => {
            expect(component).toBeTruthy()
        })

        it("form fields should be empty", () => {
            expect(component.name.value).toBe("")
            expect(component.email.value).toBe("")
            expect(component.password.value).toBe("")
            expect(component.role.value).toBe("")
        })
    })



})
