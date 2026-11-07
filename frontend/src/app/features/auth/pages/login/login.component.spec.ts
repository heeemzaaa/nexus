import { beforeEach, describe, expect, it } from "vitest";
import { LoginComponent } from "./login.component";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { provideHttpClient } from "@angular/common/http";
import { provideRouter } from "@angular/router";

describe("LoginComponent", () => {
    let component: LoginComponent
    let fixture: ComponentFixture<LoginComponent>

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [LoginComponent],
            providers: [provideHttpClient(), provideRouter([])]
        }).compileComponents()

        fixture = TestBed.createComponent(LoginComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    })

    describe("component setup", () => {
        it("should create", () => {
            expect(component).toBeTruthy()
        })

        it("form fields should be empty", () => {
            expect(component.email.value).toBe("")
            expect(component.password.value).toBe("")
        })
    })

})